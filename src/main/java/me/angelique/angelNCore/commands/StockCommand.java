package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.StockExchangeService.CompanyInfo;
import me.angelique.angelNCore.services.StockExchangeService.OrderInfo;
import me.angelique.angelNCore.services.ServiceRegistry;
import me.angelique.angelNCore.services.impl.StockExchangeServiceImpl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StockCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("\u00a7cPlayers only.");
            return true;
        }
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        if (ex == null) {
            sender.sendMessage("\u00a7cStock exchange unavailable.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            List<CompanyInfo> companies = ex.listCompanies();
            p.sendMessage("\u00a76\u00a7l=== Listed Companies ===\u00a7r");
            if (companies.isEmpty()) {
                p.sendMessage("\u00a77No companies listed yet.");
            } else {
                for (CompanyInfo c : companies) {
                    p.sendMessage("\u00a7e" + c.name() + "\u00a77 (" + trim(c.companyId(), 8) + ") \u00a7f$" +
                            String.format("%.2f", c.currentPrice()) + " \u00a77| " + c.totalShares() + " shares");
                }
            }
            p.sendMessage("\u00a77/stock buy <company> <shares> <price> | /stock sell <company> <shares> <price>");
            p.sendMessage("\u00a77/stock portfolio | /stock orders <company> | /stock token");
            p.sendMessage("\u00a77Trade online: \u00a7bhttp://127.0.0.1:8080/app/");
            return true;
        }

        String sub = args[0].toLowerCase();
        UUID playerUUID = p.getUniqueId();

        if (sub.equals("portfolio") || sub.equals("pf")) {
            p.sendMessage("\u00a76\u00a7l=== Your Portfolio ===\u00a7r");
            double total = 0;
            boolean hasAny = false;
            for (CompanyInfo c : ex.listCompanies()) {
                int shares = ex.getHolding(playerUUID, c.companyId());
                if (shares > 0) {
                    double val = shares * c.currentPrice();
                    total += val;
                    hasAny = true;
                    p.sendMessage("\u00a7e" + c.name() + "\u00a7f: " + shares + " shares \u00a77(\u00a7a$" +
                            String.format("%.2f", val) + "\u00a77)");
                }
            }
            if (!hasAny) p.sendMessage("\u00a77You own no shares.");
            if (total > 0) p.sendMessage("\u00a7aTotal value: \u00a7f$" + String.format("%.2f", total));
            return true;
        }

        if (sub.equals("token")) {
            if (ex instanceof StockExchangeServiceImpl se) {
                String token = se.generateToken(playerUUID);
                if (!token.isEmpty()) {
                    p.sendMessage("\u00a7aYour web auth token: \u00a7f" + token);
                    p.sendMessage("\u00a77Go to \u00a7bhttp://127.0.0.1:8080/app/ \u00a77— enter your UUID and this token.");
                    p.sendMessage("\u00a77Expires in 1 hour.");
                } else {
                    p.sendMessage("\u00a7cFailed to generate token.");
                }
            }
            return true;
        }

        if ((sub.equals("buy") || sub.equals("sell")) && args.length >= 4) {
            String companyId = args[1];
            if (!ex.isListed(companyId)) { p.sendMessage("\u00a7cUnknown company: " + companyId); return true; }
            int shares;
            double price;
            try {
                shares = Integer.parseInt(args[2]);
                price = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage("\u00a7cInvalid number.");
                return true;
            }
            if (shares <= 0) { p.sendMessage("\u00a7cShares must be > 0."); return true; }
            if (price <= 0) { p.sendMessage("\u00a7cPrice must be > 0."); return true; }

            String orderId = ex.placeOrder(playerUUID, companyId, sub, shares, price);
            p.sendMessage("\u00a7a" + sub.toUpperCase() + " order placed! " + shares + " shares @ $" +
                    String.format("%.2f", price) + " (\u00a77ID: " + orderId.substring(0, 8) + "...\u00a7a)");
            return true;
        }

        if (sub.equals("orders") && args.length >= 2) {
            String cid = args[1];
            List<OrderInfo> orders = ex.getOrderBook(cid);
            p.sendMessage("\u00a76\u00a7l=== Order Book: " + cid + " ===\u00a7r");
            if (orders.isEmpty()) {
                p.sendMessage("\u00a77No open orders.");
            } else {
                for (OrderInfo o : orders) {
                    String color = o.type().equals("buy") ? "\u00a7a" : "\u00a7c";
                    p.sendMessage(color + o.type().toUpperCase() + "\u00a7f: " + o.shares() + " @ $" +
                            String.format("%.2f", o.price()) + " \u00a77(" + trim(o.orderId(), 8) + ")");
                }
            }
            return true;
        }

        p.sendMessage("\u00a7cUsage: /stock <list|portfolio|buy|sell|orders> [args]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> opts = new ArrayList<>();
        if (args.length == 1) {
            opts.addAll(List.of("list", "portfolio", "pf", "buy", "sell", "orders", "token"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("orders"))) {
            StockExchangeService ex = ServiceRegistry.getStockExchangeService();
            if (ex != null) for (var c : ex.listCompanies()) opts.add(c.companyId());
        }
        return opts;
    }

    private String trim(String s, int len) { return s.length() > len ? s.substring(0, len) : s; }
}
