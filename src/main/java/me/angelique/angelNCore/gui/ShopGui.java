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

    public static final String TITLE = TextUtil.color("&aBuy & Sell");
    public static final String DETAIL_TITLE = TextUtil.color("&eSelect Amount");
    static final int SIZE = 54;
    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };
    private static final int[] BUY_AMOUNTS = {1, 8, 16, 32, 64};
    private static final int[] BUY_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] SELL_AMOUNTS = {1, 8, 16, 32, 64};
    private static final int[] SELL_SLOTS = {29, 30, 31, 32, 33};

    static final Map<UUID, String> selectedItem = new HashMap<>();

    private ShopGui() {}

    public static void open(Player player, AngelNCore plugin, int page) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        economy.initPlayer(player);

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.GOLD_NUGGET, "&6Balance: &e" + economy.formatBalance(economy.getBalance(player.getUniqueId())),
                "&7Prices shift with supply and demand"));

        List<Map.Entry<String, double[]>> items = new ArrayList<>(market.getAllPrices().entrySet());
        items.sort(Map.Entry.comparingByKey());

        int perPage = ITEM_SLOTS.length;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / perPage));
        page = Math.max(0, Math.min(page, totalPages - 1));
        int start = page * perPage;
        int end = Math.min(start + perPage, items.size());

        for (int i = start; i < end; i++) {
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
                        TextUtil.color("&aBuy: &f$" + String.format("%.2f", buyPrice) + " each"),
                        TextUtil.color("&eSell: &f$" + String.format("%.2f", sellPrice) + " each"),
                        "",
                        TextUtil.color("&eClick to select amount")
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(ITEM_SLOTS[i - start], item);
        }

        if (page > 0) inv.setItem(45, item(Material.ARROW, "&ePrevious", "&7Page " + page + " of " + totalPages));
        if (page < totalPages - 1) inv.setItem(53, item(Material.ARROW, "&eNext", "&7Page " + (page + 2) + " of " + totalPages));
        inv.setItem(49, item(Material.KNOWLEDGE_BOOK, "&7Page " + (page + 1) + " of " + totalPages,
                "&7Click an item to buy or sell"));

        player.openInventory(inv);
    }

    public static void openDetail(Player player, AngelNCore plugin, String itemKey) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        Inventory inv = Bukkit.createInventory(null, 45, DETAIL_TITLE);
        fillBorder45(inv);
        selectedItem.put(player.getUniqueId(), itemKey);

        double buyPrice = market.getCurrentPrice(itemKey);
        double sellPrice = market.getSellPrice(itemKey);
        Material mat = Material.getMaterial(itemKey);
        String displayName = market.getDisplayName(itemKey);
        double balance = economy.getBalance(player.getUniqueId());
        int held = 0;
        if (mat != null) {
            for (ItemStack s : player.getInventory().getContents()) {
                if (s != null && s.getType() == mat) held += s.getAmount();
            }
        }

        // Item info in center
        inv.setItem(13, item(mat != null ? mat : Material.PAPER, "&f" + displayName,
                "&aBuy price: &f$" + String.format("%.2f", buyPrice),
                "&eSell price: &f$" + String.format("%.2f", sellPrice),
                "&6Your balance: &f$" + String.format("%.2f", balance),
                "&7You have: &f" + held));

        // Buy buttons centered in row 2 (slots 11-15)
        for (int i = 0; i < BUY_AMOUNTS.length; i++) {
            double cost = buyPrice * BUY_AMOUNTS[i];
            boolean canAfford = balance >= cost;
            inv.setItem(BUY_SLOTS[i], item(canAfford ? Material.LIME_TERRACOTTA : Material.GRAY_TERRACOTTA,
                    "&aBuy " + BUY_AMOUNTS[i],
                    canAfford ? "&7Cost: &f$" + String.format("%.2f", cost) : "&cCannot afford",
                    canAfford ? "&eClick to buy" : ""));
        }

        // Sell buttons centered in row 4 (slots 29-33)
        for (int i = 0; i < SELL_AMOUNTS.length; i++) {
            double value = sellPrice * SELL_AMOUNTS[i];
            boolean canSell = held >= SELL_AMOUNTS[i];
            inv.setItem(SELL_SLOTS[i], item(canSell ? Material.RED_TERRACOTTA : Material.GRAY_TERRACOTTA,
                    "&cSell " + SELL_AMOUNTS[i],
                    canSell ? "&7Value: &f$" + String.format("%.2f", value) : "&cNot enough",
                    canSell ? "&eClick to sell" : ""));
        }

        inv.setItem(40, item(Material.OAK_DOOR, "&cBack", "&7Return to item list"));
        player.openInventory(inv);
    }

    public static void handleBuy(Player player, AngelNCore plugin, String itemKey, int amount) {
        EconomyManager economy = plugin.getEconomyManager();
        MarketManager market = plugin.getMarketManager();
        economy.initPlayer(player);
        double priceEach = market.getCurrentPrice(itemKey);
        double total = priceEach * amount;
        if (!economy.has(player.getUniqueId(), total)) {
            player.sendMessage(TextUtil.color("&cNot enough money. Need $" + String.format("%.2f", total)));
            return;
        }
        Material mat = Material.getMaterial(itemKey);
        if (mat == null) return;
        economy.withdraw(player.getUniqueId(), total);
        market.recordBuy(itemKey, amount, player.getUniqueId(), player.getName(), priceEach);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(mat, amount));
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(TextUtil.color("&aBought " + amount + "x " + market.getDisplayName(itemKey) + " for $" + String.format("%.2f", total)));
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
            player.sendMessage(TextUtil.color("&cYou only have " + held + "x."));
            return;
        }
        double priceEach = market.getSellPrice(itemKey);
        double total = priceEach * amount;
        player.getInventory().removeItem(new ItemStack(mat, amount));
        economy.deposit(player.getUniqueId(), total);
        market.recordSell(itemKey, amount, player.getUniqueId(), player.getName(), priceEach);
        player.sendMessage(TextUtil.color("&aSold " + amount + "x " + market.getDisplayName(itemKey) + " for $" + String.format("%.2f", total)));
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    static void fillBorder45(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, glass);
    }

    static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(lore).filter(l -> !l.isEmpty()).map(TextUtil::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    static ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }
}
