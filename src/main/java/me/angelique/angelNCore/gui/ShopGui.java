package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.EconomyManager;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class ShopGui {

    public static final String TITLE = TextUtil.color("&8Server Shop &7\u2014 &aBuy & Sell");
    public static final int SIZE = 54;
    private static final int ITEMS_PER_PAGE = 28;
    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    private ShopGui() {}

    public static void open(Player player, AngelNCore plugin, int page) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        economy.initPlayer(player);

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        ItemStack glass = AngelHubGui.pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);

        // Header
        inv.setItem(4, AngelHubGui.icon(Material.GOLD_NUGGET, "&6Balance: &e" + economy.formatBalance(economy.getBalance(player.getUniqueId())),
                "&7Prices shift with every trade"));

        // Get all items and sort
        List<Map.Entry<String, double[]>> items = new ArrayList<>(market.getAllPrices().entrySet());
        items.sort(Map.Entry.comparingByKey());

        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, items.size());

        for (int i = startIdx; i < endIdx; i++) {
            Map.Entry<String, double[]> entry = items.get(i);
            String key = entry.getKey();
            double buyPrice = entry.getValue()[0];
            double sellPrice = entry.getValue()[1];
            Material mat = Material.getMaterial(key);
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(TextUtil.color("&f" + market.getDisplayName(key)));
                meta.setLore(Arrays.asList(
                        TextUtil.color("&aBuy: &f" + economy.formatBalance(buyPrice) + " &7each"),
                        TextUtil.color("&eSell: &f" + economy.formatBalance(sellPrice) + " &7each"),
                        "",
                        TextUtil.color("&eLeft-click &7Buy 1"),
                        TextUtil.color("&eShift+Left &7Buy 64"),
                        TextUtil.color("&eRight-click &7Sell 1"),
                        TextUtil.color("&eShift+Right &7Sell 64")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(ITEM_SLOTS[i - startIdx], item);
        }

        // Navigation
        if (page > 0)
            inv.setItem(45, AngelHubGui.icon(Material.ARROW, "&e\u2190 Previous Page", "&7Page " + (page) + " of " + totalPages));
        if (page < totalPages - 1)
            inv.setItem(53, AngelHubGui.icon(Material.ARROW, "&eNext Page \u2192", "&7Page " + (page + 2) + " of " + totalPages));

        inv.setItem(48, AngelHubGui.icon(Material.OAK_DOOR, "&cBack to Menu", "&7Return to hub"));
        inv.setItem(49, AngelHubGui.icon(Material.KNOWLEDGE_BOOK, "&7Page " + (page + 1) + " of " + totalPages,
                "&7Click items to trade"));

        player.openInventory(inv);
    }

    public static void handleBuy(Player player, AngelNCore plugin, String itemKey, int amount) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        economy.initPlayer(player);

        if (!market.isValidItem(itemKey)) {
            player.sendMessage(TextUtil.color("&cItem not available."));
            return;
        }

        double priceEach = market.getCurrentPrice(itemKey);
        double total = priceEach * amount;

        if (!economy.has(player.getUniqueId(), total)) {
            player.sendMessage(TextUtil.color("&cNot enough money. Need: " + economy.formatBalance(total)));
            return;
        }

        Material mat = Material.getMaterial(itemKey);
        if (mat == null) return;

        economy.withdraw(player.getUniqueId(), total);
        market.recordBuy(itemKey, amount, player.getUniqueId(), player.getName(), priceEach);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, amount));
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.sendMessage(TextUtil.color("&aBought &f" + amount + "x " + market.getDisplayName(itemKey)
                + " &afor " + economy.formatBalance(total)));
    }

    public static void handleSell(Player player, AngelNCore plugin, String itemKey, int amount) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        economy.initPlayer(player);

        Material mat = Material.getMaterial(itemKey);
        if (mat == null) return;

        int held = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) held += stack.getAmount();
        }

        if (held < amount) {
            player.sendMessage(TextUtil.color("&cYou only have " + held + "x " + market.getDisplayName(itemKey) + "."));
            return;
        }

        double priceEach = market.getSellPrice(itemKey);
        double total = priceEach * amount;

        player.getInventory().removeItem(new ItemStack(mat, amount));
        economy.deposit(player.getUniqueId(), total);
        market.recordSell(itemKey, amount, player.getUniqueId(), player.getName(), priceEach);

        player.sendMessage(TextUtil.color("&aSold &f" + amount + "x " + market.getDisplayName(itemKey)
                + " &afor " + economy.formatBalance(total)));
    }
}
