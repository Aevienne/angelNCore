package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.services.CrossListingService;
import me.angelique.angelNCore.services.MarketService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MarketService ms = ServiceRegistry.getMarketService();
        CrossListingService cls = ServiceRegistry.getCrossListingService();

        if (args.length > 0 && args[0].equalsIgnoreCase("info") && args.length > 1) {
            String item = args[1].toUpperCase();
            if (cls != null) {
                String report = cls.getLiquidityReport(item);
                sender.sendMessage("\u00a7e" + report);
            }
            if (ms != null) {
                double price = ms.getPrice(item);
                sender.sendMessage("\u00a7aMarket price for " + item + ": \u00a7f$" + String.format("%.2f", price));
                sender.sendMessage("\u00a77Sell to shop: \u00a7f$" + String.format("%.2f", price * 0.7));
            }
            return true;
        }

        // Show all prices
        sender.sendMessage("\u00a76\u00a7l=== AngelNetwork Market Prices ===\u00a7r");
        if (ms != null) {
            Map<String, Double> prices = ms.getAllPrices();
            if (prices.isEmpty()) {
                sender.sendMessage("\u00a77No items listed yet.");
            } else {
                for (var entry : prices.entrySet()) {
                    double crossPrice = cls != null ? cls.getBestPrice(entry.getKey(), 1) : entry.getValue();
                    sender.sendMessage("\u00a7e" + entry.getKey() + "\u00a7f: $" + String.format("%.2f", entry.getValue()) +
                            "  \u00a77(best cross-venue: $" + String.format("%.2f", crossPrice) + ")");
                }
            }
        } else {
            sender.sendMessage("\u00a7cMarket service unavailable.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>();
            opts.add("info");
            return opts;
        }
        return new ArrayList<>();
    }
}
