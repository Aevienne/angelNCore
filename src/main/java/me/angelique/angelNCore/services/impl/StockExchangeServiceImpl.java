package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.StockExchangeService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class StockExchangeServiceImpl implements StockExchangeService {

    private final AngelNCore plugin;
    private final List<PriceCandle> recentCandles = new CopyOnWriteArrayList<>();

    public StockExchangeServiceImpl(AngelNCore plugin) {
        this.plugin = plugin;
    }

    private Connection conn() throws SQLException {
        return plugin.getDatabaseManager().getConnection();
    }

    @Override
    public void listCompany(String companyId, int totalShares, double initialPrice) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT OR REPLACE INTO listed_companies (company_id, total_shares, current_price, listed_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, companyId);
            ps.setInt(2, totalShares);
            ps.setDouble(3, initialPrice);
            ps.setLong(4, System.currentTimeMillis());
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
                "SELECT l.company_id, c.name, l.total_shares, l.current_price, " +
                "(SELECT COALESCE(SUM(CASE WHEN type='buy' AND status='filled' THEN shares ELSE 0 END),0) FROM stock_orders WHERE company_id=?), " +
                "l.total_shares FROM listed_companies l LEFT JOIN players p ON p.uuid=l.company_id WHERE l.company_id=?")) {
            ps.setString(1, companyId);
            ps.setString(2, companyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CompanyInfo(rs.getString(1), "Company " + companyId.substring(0, 8), rs.getInt(3), rs.getDouble(4), rs.getInt(5));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getCompanyInfo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<CompanyInfo> listCompanies() {
        List<CompanyInfo> list = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT company_id, total_shares, current_price FROM listed_companies")) {
            while (rs.next()) {
                String cid = rs.getString(1);
                list.add(new CompanyInfo(cid, "Company " + cid.substring(0, 8), rs.getInt(2), rs.getDouble(3), 0));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("listCompanies: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<PriceCandle> getPriceHistory(String companyId) {
        List<PriceCandle> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT timestamp, open, high, low, close, volume FROM stock_price_history WHERE company_id=? ORDER BY timestamp DESC LIMIT 168")) {
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

    @Override
    public String placeOrder(UUID playerUUID, String companyId, String type, int shares, double price) {
        String orderId = UUID.randomUUID().toString();
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO stock_orders (order_id, company_id, player_uuid, type, shares, price, status, placed_at) VALUES (?,?,?,?,?,?,'open',?)")) {
            ps.setString(1, orderId);
            ps.setString(2, companyId);
            ps.setString(3, playerUUID.toString());
            ps.setString(4, type);
            ps.setInt(5, shares);
            ps.setDouble(6, price);
            ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();
            matchOrders(companyId);
        } catch (SQLException e) {
            plugin.getLogger().warning("placeOrder: " + e.getMessage());
        }
        return orderId;
    }

    @Override
    public boolean cancelOrder(String orderId, UUID playerUUID) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE stock_orders SET status='cancelled' WHERE order_id=? AND player_uuid=? AND status='open'")) {
            ps.setString(1, orderId);
            ps.setString(2, playerUUID.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            // ignore
        }
        return 0;
    }

    private void matchOrders(String companyId) {
        try {
            List<OrderInfo> buys = new ArrayList<>();
            List<OrderInfo> sells = new ArrayList<>();
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT order_id, player_uuid, type, shares, price FROM stock_orders WHERE company_id=? AND status='open' ORDER BY price DESC")) {
                ps.setString(1, companyId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    OrderInfo o = new OrderInfo(rs.getString(1), companyId, UUID.fromString(rs.getString(2)), rs.getString(3), rs.getInt(4), rs.getDouble(5), "open");
                    if ("buy".equals(o.type())) buys.add(o);
                    else sells.add(o);
                }
            }
            // Sort: buy highest price first, sell lowest price first
            buys.sort((a, b) -> Double.compare(b.price(), a.price()));
            sells.sort(Comparator.comparingDouble(OrderInfo::price));

            for (OrderInfo buy : new ArrayList<>(buys)) {
                for (OrderInfo sell : new ArrayList<>(sells)) {
                    if (buy.price() >= sell.price() && buy.shares() > 0 && sell.shares() > 0) {
                        int match = Math.min(buy.shares(), sell.shares());
                        double execPrice = sell.price();
                        executeTrade(buy.orderId(), sell.orderId(), companyId, match, execPrice);
                        buys.remove(buy);
                        sells.remove(sell);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("matchOrders: " + e.getMessage());
        }
    }

    private void executeTrade(String buyId, String sellId, String companyId, int shares, double price) {
        try {
            conn().setAutoCommit(false);
            UUID buyer = null, seller = null;
            try (PreparedStatement ps = conn().prepareStatement("SELECT order_id,player_uuid FROM stock_orders WHERE order_id=?")) {
                ps.setString(1, buyId);
                ResultSet rs = ps.executeQuery(); if (rs.next()) buyer = UUID.fromString(rs.getString(2));
            }
            try (PreparedStatement ps = conn().prepareStatement("SELECT order_id,player_uuid FROM stock_orders WHERE order_id=?")) {
                ps.setString(1, sellId);
                ResultSet rs = ps.executeQuery(); if (rs.next()) seller = UUID.fromString(rs.getString(2));
            }
            if (buyer == null || seller == null) { conn().rollback(); conn().setAutoCommit(true); return; }

            // Update holdings
            upsertHolding(buyer, companyId, shares);
            upsertHolding(seller, companyId, -shares);

            // Update orders
            updateOrderAfterTrade(buyId, shares);
            updateOrderAfterTrade(sellId, shares);

            // Update price
            try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET current_price=? WHERE company_id=?")) {
                ps.setDouble(1, price);
                ps.setString(2, companyId);
                ps.executeUpdate();
            }

            conn().commit();
            conn().setAutoCommit(true);

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
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ignored) {}
            plugin.getLogger().warning("executeTrade: " + e.getMessage());
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

    private void updateOrderAfterTrade(String orderId, int matched) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT shares FROM stock_orders WHERE order_id=?")) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int remaining = rs.getInt(1) - matched;
                if (remaining <= 0) {
                    try (PreparedStatement u = conn().prepareStatement("UPDATE stock_orders SET status='filled',shares=0 WHERE order_id=?")) {
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
    public void applyDamageModifier(String companyId, double factor) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE listed_companies SET current_price=current_price*? WHERE company_id=?")) {
            ps.setDouble(1, factor);
            ps.setString(2, companyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("applyDamageModifier: " + e.getMessage());
        }
    }

    @Override
    public void applyWarModifier(String companyId, double factor) {
        applyDamageModifier(companyId, factor);
    }
}
