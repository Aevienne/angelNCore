package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.EconomyManager;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.gui.ShopGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ShopCommand implements CommandExecutor {

    private final AngelNCore plugin;
    private final EconomyManager economy;
    private final MarketManager market;

    public ShopCommand(AngelNCore plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomyManager();
        this.market = plugin.getMarketManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        economy.initPlayer(player);

        if (args.length == 0) {
            ShopGui.open(player, plugin, 0);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sendPriceList(player);
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("buy") || action.equals("sell")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /shop " + action + " <item> [amount]");
                return true;
            }

            String item = args[1].toUpperCase();
            int amount = 1;

            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Amount must be a positive number.");
                    return true;
                }
            }

            if (!market.isValidItem(item)) {
                player.sendMessage(ChatColor.RED + "That item is not available in the shop.");
                player.sendMessage(ChatColor.YELLOW + "Use /shop list to see available items.");
                return true;
            }

            if (action.equals("buy")) handleBuy(player, item, amount);
            else handleSell(player, item, amount);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Usage: /shop [buy|sell|list] [item] [amount]");
        return true;
    }

    private void handleBuy(Player player, String item, int amount) {
        double priceEach = market.getCurrentPrice(item);
        double total = priceEach * amount;

        if (!economy.has(player.getUniqueId(), total)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money!");
            player.sendMessage(ChatColor.RED + "Need: " + economy.formatBalance(total)
                    + " | Balance: " + economy.formatBalance(economy.getBalance(player.getUniqueId())));
            return;
        }

        Material mat = Material.getMaterial(item);
        if (mat == null) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        economy.withdraw(player.getUniqueId(), total);
        market.recordBuy(item, amount, player.getUniqueId(), player.getName(), priceEach);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, amount));
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
            player.sendMessage(ChatColor.YELLOW + "Some items dropped at your feet (inventory full).");
        }

        player.sendMessage(ChatColor.GREEN + "Bought " + ChatColor.WHITE + amount + "x "
                + market.getDisplayName(item) + ChatColor.GREEN + " for " + economy.formatBalance(total));
        player.sendMessage(ChatColor.GRAY + "New price: " + economy.formatBalance(market.getCurrentPrice(item))
                + " each (demand increased)");
    }

    private void handleSell(Player player, String item, int amount) {
        Material mat = Material.getMaterial(item);
        if (mat == null) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        int held = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) held += stack.getAmount();
        }

        if (held < amount) {
            player.sendMessage(ChatColor.RED + "You only have " + held + "x " + market.getDisplayName(item) + ".");
            return;
        }

        double priceEach = market.getSellPrice(item);
        double total = priceEach * amount;

        player.getInventory().removeItem(new ItemStack(mat, amount));
        economy.deposit(player.getUniqueId(), total);
        market.recordSell(item, amount, player.getUniqueId(), player.getName(), priceEach);

        player.sendMessage(ChatColor.GREEN + "Sold " + ChatColor.WHITE + amount + "x "
                + market.getDisplayName(item) + ChatColor.GREEN + " for " + economy.formatBalance(total));
        player.sendMessage(ChatColor.GRAY + "New buy price: " + economy.formatBalance(market.getCurrentPrice(item))
                + " each (supply increased)");
    }

    private void sendPriceList(Player player) {
        player.sendMessage(ChatColor.GOLD + "━━━━━━━ " + ChatColor.YELLOW + "Market Prices" + ChatColor.GOLD + " ━━━━━━━");
        player.sendMessage(ChatColor.GRAY + String.format("%-18s %-13s %s", "Item", "Buy", "Sell"));
        player.sendMessage(ChatColor.GOLD + "─────────────────────────────────────────");

        for (Map.Entry<String, double[]> entry : market.getAllPrices().entrySet()) {
            String name = market.getDisplayName(entry.getKey());
            double buy = entry.getValue()[0];
            double sell = entry.getValue()[1];
            player.sendMessage(ChatColor.WHITE + String.format("%-18s", name)
                    + ChatColor.GREEN + String.format("%-13s", economy.formatBalance(buy))
                    + ChatColor.YELLOW + economy.formatBalance(sell));
        }

        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.GRAY + "Prices shift with every buy and sell!");
    }
}
