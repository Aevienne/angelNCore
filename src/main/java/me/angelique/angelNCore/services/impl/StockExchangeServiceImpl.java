package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.api.StockApiServer;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.economy.EconomyManager;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class StockExchangeServiceImpl implements StockExchangeService {

    private final AngelNCore plugin;
    private final List<PriceCandle> recentCandles = new CopyOnWriteArrayList<>();
    private StockApiServer apiServer;

    public StockExchangeServiceImpl(AngelNCore plugin) {
        this.plugin = plugin;
    }

    public void setApiServer(StockApiServer server) { this.apiServer = server; }

    private Connection conn() throws SQLException {
        return plugin.getDatabaseManager().getConnection();
    }

    // ── Company Management ─────────────────────────────────────────────────

    @Override
    public void listCompany(String companyId, String companyName, int totalShares, double initialPrice) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT OR REPLACE INTO listed_companies (company_id, name, total_shares, current_price, listed_at, damage_factor, war_factor, logistics_factor, season_factor) VALUES (?, ?, ?, ?, ?, 1.0, 1.0, 1.0, 1.0)")) {
            ps.setString(1, companyId);
            ps.setString(2, companyName);
            ps.setInt(3, totalShares);
            ps.setDouble(4, initialPrice);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("listCompany failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isListed(String companyId) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM listed_companies WHERE company_id = ?")) {
            ps.setString(1, companyId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public CompanyInfo getCompanyInfo(String companyId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT l.company_id, l.name, l.total_shares, l.current_price, l.damage_factor, l.war_factor, l.logistics_factor, l.season_factor, " +
                "COALESCE((SELECT SUM(r.revenue) FROM company_revenue r WHERE r.company_id=l.company_id AND r.date >= ?),0) " +
                "FROM listed_companies l WHERE l.company_id=?")) {
            ps.setString(1, java.time.LocalDate.now().minusDays(7).toString());
            ps.setString(2, companyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double baseVal = rs.getDouble(9) / 100.0;
                double effectivePrice = baseVal * rs.getDouble(5) * rs.getDouble(6) * rs.getDouble(7) * rs.getDouble(8);
                if (effectivePrice <= 0) effectivePrice = rs.getDouble(4);
                String name = rs.getString(2);
                if (name == null || name.isEmpty()) name = "Company " + trim(companyId);
                return new CompanyInfo(rs.getString(1), name, rs.getInt(3), effectivePrice, 0);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getCompanyInfo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<CompanyInfo> listCompanies() {
        List<CompanyInfo> list = new ArrayList<>();
        String sevenDaysAgo = java.time.LocalDate.now().minusDays(7).toString();
        record Row(String cid, String name, int shares, double price, double df, double wf, double lf, double sf) {}
        List<Row> rows = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT company_id, name, total_shares, current_price, damage_factor, war_factor, logistics_factor, season_factor FROM listed_companies")) {
            while (rs.next()) {
                rows.add(new Row(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7), rs.getDouble(8)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("listCompanies: " + e.getMessage());
        }
        for (Row r : rows) {
            double baseVal = 0;
            try (PreparedStatement ps = conn().prepareStatement("SELECT COALESCE(SUM(revenue),0) FROM company_revenue WHERE company_id=? AND date >= ?")) {
                ps.setString(1, r.cid());
                ps.setString(2, sevenDaysAgo);
                ResultSet rs2 = ps.executeQuery();
                if (rs2.next()) baseVal = rs2.getDouble(1) / 100.0;
            } catch (SQLException ignored) {}
            double effectivePrice = baseVal * r.df() * r.wf() * r.lf() * r.sf();
            if (effectivePrice <= 0) effectivePrice = r.price();
            String name = r.name();
            if (name == null || name.isEmpty()) name = "Company " + trim(r.cid());
            list.add(new CompanyInfo(r.cid(), name, r.shares(), effectivePrice, 0));
        }
        return list;
    }

    @Override
    public List<PriceCandle> getPriceHistory(String companyId) {
        List<PriceCandle> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT timestamp, open, high, low, close, volume FROM (" +
                "SELECT timestamp, open, high, low, close, volume FROM stock_price_history WHERE company_id=? ORDER BY timestamp DESC LIMIT 168" +
                ") ORDER BY timestamp ASC")) {
            ps.setString(1, companyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PriceCandle(rs.getLong(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getInt(6)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getPriceHistory: " + e.getMessage());
        }
        return list;
    }

    // ── Order Management (Production) ───────────────────────────────────────

    @Override
    public String placeOrder(UUID playerUUID, String companyId, String type, int shares, double price) {
        EconomyManager econ = plugin.getEconomyManager();
        String orderId = UUID.randomUUID().toString();

        if (!isListed(companyId)) {
            plugin.getLogger().warning("placeOrder rejected: company " + companyId + " not listed");
            return "";
        }

        if ("buy".equals(type)) {
            double total = shares * price;
            if (!econ.withdraw(playerUUID, total)) {
                plugin.getLogger().warning("placeOrder rejected: " + playerUUID + " has insufficient funds ($" + total + ")");
                return "";
            }
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO stock_orders (order_id, company_id, player_uuid, type, shares, price, locked_funds, status, placed_at) VALUES (?,?,?,?,?,?,?,'open',?)")) {
                ps.setString(1, orderId);
                ps.setString(2, companyId);
                ps.setString(3, playerUUID.toString());
                ps.setString(4, type);
                ps.setInt(5, shares);
                ps.setDouble(6, price);
                ps.setDouble(7, total);
                ps.setLong(8, System.currentTimeMillis());
                ps.executeUpdate();
                plugin.getLogger().info("BUY order " + orderId.substring(0, 8) + ": " + shares + " @ $" + price + " — $" + total + " locked");
            } catch (SQLException e) {
                econ.deposit(playerUUID, total); // refund on failure
                plugin.getLogger().warning("placeOrder INSERT failed: " + e.getMessage());
                return "";
            }
        } else {
            int holding = getHolding(playerUUID, companyId);
            if (holding < shares) {
                plugin.getLogger().warning("placeOrder rejected: " + playerUUID + " has only " + holding + " shares of " + companyId);
                return "";
            }
            // Remove shares from holdings (they are now locked in the order)
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE stock_holdings SET shares=shares-? WHERE player_uuid=? AND company_id=?")) {
                ps.setInt(1, shares);
                ps.setString(2, playerUUID.toString());
                ps.setString(3, companyId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("placeOrder holding deduction failed: " + e.getMessage());
                return "";
            }
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO stock_orders (order_id, company_id, player_uuid, type, shares, price, locked_funds, status, placed_at) VALUES (?,?,?,?,?,?,0,'open',?)")) {
                ps.setString(1, orderId);
                ps.setString(2, companyId);
                ps.setString(3, playerUUID.toString());
                ps.setString(4, type);
                ps.setInt(5, shares);
                ps.setDouble(6, price);
                ps.setLong(7, System.currentTimeMillis());
                ps.executeUpdate();
                plugin.getLogger().info("SELL order " + orderId.substring(0, 8) + ": " + shares + " @ $" + price + " — " + shares + " shares locked");
            } catch (SQLException e) {
                // Refund shares on insert failure
                try (PreparedStatement ps2 = conn().prepareStatement(
                        "UPDATE stock_holdings SET shares=shares+? WHERE player_uuid=? AND company_id=?")) {
                    ps2.setInt(1, shares);
                    ps2.setString(2, playerUUID.toString());
                    ps2.setString(3, companyId);
                    ps2.executeUpdate();
                } catch (SQLException ex) { plugin.getLogger().warning("placeOrder share refund failed: " + ex.getMessage()); }
                plugin.getLogger().warning("placeOrder INSERT failed: " + e.getMessage());
                return "";
            }
        }

        matchOrders(companyId);
        return orderId;
    }

    @Override
    public boolean cancelOrder(String orderId, UUID playerUUID) {
        try {
            // Get order details before cancelling
            String type = "buy";
            double lockedFunds = 0;
            int shares = 0;
            String companyId = "";
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT type, locked_funds, shares, company_id FROM stock_orders WHERE order_id=? AND player_uuid=? AND status='open'")) {
                ps.setString(1, orderId);
                ps.setString(2, playerUUID.toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false;
                type = rs.getString(1);
                lockedFunds = rs.getDouble(2);
                shares = rs.getInt(3);
                companyId = rs.getString(4);
            }

            // Mark as cancelled
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE stock_orders SET status='cancelled' WHERE order_id=?")) {
                ps.setString(1, orderId);
                if (ps.executeUpdate() == 0) return false;
            }

            // Refund
            EconomyManager econ = plugin.getEconomyManager();
            if ("buy".equals(type)) {
                if (lockedFunds > 0) econ.deposit(playerUUID, lockedFunds);
                plugin.getLogger().info("BUY cancelled " + orderId.substring(0, 8) + ": $" + lockedFunds + " refunded");
            } else {
                try (PreparedStatement ps = conn().prepareStatement(
                        "UPDATE stock_holdings SET shares=shares+? WHERE player_uuid=? AND company_id=?")) {
                    ps.setInt(1, shares);
                    ps.setString(2, playerUUID.toString());
                    ps.setString(3, companyId);
                    ps.executeUpdate();
                }
                plugin.getLogger().info("SELL cancelled " + orderId.substring(0, 8) + ": " + shares + " shares returned");
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("cancelOrder: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<OrderInfo> getOrderBook(String companyId) {
        List<OrderInfo> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT order_id, company_id, player_uuid, type, shares, price, status FROM stock_orders WHERE company_id=? AND status='open' ORDER BY price DESC")) {
            ps.setString(1, companyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderInfo(rs.getString(1), rs.getString(2), UUID.fromString(rs.getString(3)), rs.getString(4), rs.getInt(5), rs.getDouble(6), rs.getString(7)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getOrderBook: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int getHolding(UUID playerUUID, String companyId) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT shares FROM stock_holdings WHERE player_uuid=? AND company_id=?")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, companyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    // ── Trade Execution ──────────────────────────────────────────────────────

    private void matchOrders(String companyId) {
        try {
            List<OrderInfo> buys = new ArrayList<>();
            List<OrderInfo> sells = new ArrayList<>();
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT order_id, player_uuid, type, shares, price, locked_funds FROM stock_orders WHERE company_id=? AND status='open' ORDER BY price DESC")) {
                ps.setString(1, companyId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    OrderInfo o = new OrderInfo(rs.getString(1), companyId, UUID.fromString(rs.getString(2)), rs.getString(3), rs.getInt(4), rs.getDouble(5), "open");
                    if ("buy".equals(o.type())) buys.add(o);
                    else sells.add(o);
                }
            }
            buys.sort((a, b) -> Double.compare(b.price(), a.price()));
            sells.sort(Comparator.comparingDouble(OrderInfo::price));

            for (OrderInfo buy : new ArrayList<>(buys)) {
                for (OrderInfo sell : new ArrayList<>(sells)) {
                    if (buy.price() >= sell.price() && buy.shares() > 0 && sell.shares() > 0) {
                        int match = Math.min(buy.shares(), sell.shares());
                        double execPrice = sell.price();
                        boolean ok = executeTrade(buy.orderId(), sell.orderId(), companyId, match, execPrice);
                        if (ok) {
                            buys.remove(buy);
                            sells.remove(sell);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("matchOrders: " + e.getMessage());
        }
    }

    private boolean executeTrade(String buyId, String sellId, String companyId, int shares, double price) {
        try {
            conn().setAutoCommit(false);

            // Get buyer info
            UUID buyer = null;
            double buyLocked = 0;
            try (PreparedStatement ps = conn().prepareStatement("SELECT player_uuid, locked_funds FROM stock_orders WHERE order_id=?")) {
                ps.setString(1, buyId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) { buyer = UUID.fromString(rs.getString(1)); buyLocked = rs.getDouble(2); }
            }

            // Get seller info
            UUID seller = null;
            try (PreparedStatement ps = conn().prepareStatement("SELECT player_uuid FROM stock_orders WHERE order_id=?")) {
                ps.setString(1, sellId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) seller = UUID.fromString(rs.getString(1));
            }

            if (buyer == null || seller == null) { conn().rollback(); conn().setAutoCommit(true); return false; }

            double total = shares * price;

            // Transfer holdings
            upsertHolding(buyer, companyId, shares);

            // Update buyer's order: reduce shares and locked_funds
            updateBuyOrderAfterTrade(buyId, shares, total);

            // Update seller's order: reduce shares
            updateSellOrderAfterTrade(sellId, shares);

            // Pay the seller (from buyer's locked funds — already deducted at order placement)
            plugin.getEconomyManager().deposit(seller, total);

            // Update price
            try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET current_price=? WHERE company_id=?")) {
                ps.setDouble(1, price);
                ps.setString(2, companyId);
                ps.executeUpdate();
            }

            // Audit log
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO stock_transactions (company_id, buyer_uuid, seller_uuid, shares, price, total, timestamp) VALUES (?,?,?,?,?,?,?)")) {
                ps.setString(1, companyId);
                ps.setString(2, buyer.toString());
                ps.setString(3, seller.toString());
                ps.setInt(4, shares);
                ps.setDouble(5, price);
                ps.setDouble(6, total);
                ps.setLong(7, System.currentTimeMillis());
                ps.executeUpdate();
            }

            conn().commit();
            conn().setAutoCommit(true);

            if (apiServer != null) apiServer.broadcastPrice(companyId, price);

            // Price history
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO stock_price_history (company_id, timestamp, open, high, low, close, volume) VALUES (?,?,?,?,?,?,?)")) {
                ps.setString(1, companyId);
                ps.setLong(2, System.currentTimeMillis());
                ps.setDouble(3, price);
                ps.setDouble(4, price);
                ps.setDouble(5, price);
                ps.setDouble(6, price);
                ps.setInt(7, shares);
                ps.executeUpdate();
            }

            plugin.getLogger().info("TRADE: " + shares + " shares of " + companyId + " @ $" + price +
                    " | buyer=" + buyer.toString().substring(0, 8) + " seller=" + seller.toString().substring(0, 8));
            return true;
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ignored) {}
            plugin.getLogger().warning("executeTrade FAILED: " + e.getMessage());
            return false;
        }
    }

    private void upsertHolding(UUID player, String companyId, int delta) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO stock_holdings (player_uuid, company_id, shares) VALUES (?,?,?) ON CONFLICT(player_uuid,company_id) DO UPDATE SET shares=shares+?")) {
            ps.setString(1, player.toString());
            ps.setString(2, companyId);
            ps.setInt(3, delta);
            ps.setInt(4, delta);
            ps.executeUpdate();
        }
    }

    private void updateBuyOrderAfterTrade(String orderId, int matched, double totalMatched) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT shares, locked_funds FROM stock_orders WHERE order_id=?")) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int remaining = rs.getInt(1) - matched;
                double remainingFunds = rs.getDouble(2) - totalMatched;
                if (remaining <= 0) {
                    try (PreparedStatement u = conn().prepareStatement(
                            "UPDATE stock_orders SET status='filled', shares=0, locked_funds=0 WHERE order_id=?")) {
                        u.setString(1, orderId); u.executeUpdate();
                    }
                } else {
                    // Refund excess locked funds for partial fill
                    if (remainingFunds > 0) {
                        try (PreparedStatement p2 = conn().prepareStatement("SELECT player_uuid FROM stock_orders WHERE order_id=?")) {
                            p2.setString(1, orderId);
                            ResultSet r2 = p2.executeQuery();
                            if (r2.next()) {
                                plugin.getEconomyManager().deposit(UUID.fromString(r2.getString(1)), remainingFunds - (remaining * (totalMatched / ((double) matched + remaining))));
                            }
                        }
                    }
                    try (PreparedStatement u = conn().prepareStatement(
                            "UPDATE stock_orders SET shares=?, locked_funds=? WHERE order_id=?")) {
                        u.setInt(1, remaining);
                        u.setDouble(2, remaining * (totalMatched / matched));
                        u.setString(3, orderId);
                        u.executeUpdate();
                    }
                }
            }
        }
    }

    private void updateSellOrderAfterTrade(String orderId, int matched) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT shares FROM stock_orders WHERE order_id=?")) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int remaining = rs.getInt(1) - matched;
                if (remaining <= 0) {
                    try (PreparedStatement u = conn().prepareStatement("UPDATE stock_orders SET status='filled', shares=0 WHERE order_id=?")) {
                        u.setString(1, orderId); u.executeUpdate();
                    }
                } else {
                    try (PreparedStatement u = conn().prepareStatement("UPDATE stock_orders SET shares=? WHERE order_id=?")) {
                        u.setInt(1, remaining); u.setString(2, orderId); u.executeUpdate();
                    }
                }
            }
        }
    }

    // ── Revenue & Modifiers ──────────────────────────────────────────────────

    @Override
    public void recordRevenue(String companyId, double revenue) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO company_revenue (company_id, date, revenue) VALUES (?,?,?) ON CONFLICT(company_id,date) DO UPDATE SET revenue=revenue+?")) {
            ps.setString(1, companyId);
            ps.setString(2, java.time.LocalDate.now().toString());
            ps.setDouble(3, revenue);
            ps.setDouble(4, revenue);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("recordRevenue: " + e.getMessage());
        }
    }

    @Override
    public void distributeDividends(String companyId, double amount) {
        if (amount <= 0) return;
        int totalShares = 0;
        try (PreparedStatement ps = conn().prepareStatement("SELECT total_shares FROM listed_companies WHERE company_id=?")) {
            ps.setString(1, companyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalShares = rs.getInt(1);
        } catch (SQLException e) { return; }
        if (totalShares <= 0) return;

        double dividendPool = amount * 0.10;
        List<Object[]> recipients = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT player_uuid, shares FROM stock_holdings WHERE company_id=? AND shares>0")) {
            ps.setString(1, companyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                recipients.add(new Object[]{UUID.fromString(rs.getString(1)), rs.getInt(2)});
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("distributeDividends: " + e.getMessage());
            return;
        }
        for (Object[] r : recipients) {
            UUID player = (UUID) r[0];
            int sh = (int) r[1];
            double share = dividendPool * ((double) sh / totalShares);
            if (share > 0.001) plugin.getEconomyManager().deposit(player, share);
        }
    }

    @Override
    public void applyDamageModifier(String companyId, double factor) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET damage_factor=damage_factor*? WHERE company_id=?")) {
            ps.setDouble(1, factor);
            ps.setString(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("applyDamageModifier: " + e.getMessage()); }
    }

    @Override
    public void applyWarModifier(String companyId, double factor) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET war_factor=war_factor*? WHERE company_id=?")) {
            ps.setDouble(1, factor);
            ps.setString(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("applyWarModifier: " + e.getMessage()); }
    }

    @Override
    public void applySeasonModifier(String companyId, double factor) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET season_factor=? WHERE company_id=?")) {
            ps.setDouble(1, factor);
            ps.setString(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("applySeasonModifier: " + e.getMessage()); }
    }

    public void applyLogisticsModifier(String companyId, double factor) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET logistics_factor=logistics_factor*? WHERE company_id=?")) {
            ps.setDouble(1, factor);
            ps.setString(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) { plugin.getLogger().warning("applyLogisticsModifier: " + e.getMessage()); }
    }

    // ── Web Auth Tokens ──────────────────────────────────────────────────────

    public String generateToken(UUID playerUUID) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long expires = System.currentTimeMillis() + 3600000L; // 1 hour
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT OR REPLACE INTO web_tokens (player_uuid, token, expires) VALUES (?,?,?)")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, token);
            ps.setLong(3, expires);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("generateToken: " + e.getMessage());
            return "";
        }
        return token;
    }

    public UUID validateToken(String token) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT player_uuid FROM web_tokens WHERE token=? AND expires > ?")) {
            ps.setString(1, token);
            ps.setLong(2, System.currentTimeMillis());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return UUID.fromString(rs.getString(1));
        } catch (SQLException ignored) {}
        return null;
    }

    private String trim(String s) { return s.length() > 8 ? s.substring(0, 8) : s; }
}
