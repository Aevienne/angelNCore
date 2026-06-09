package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.StockExchangeService.CompanyInfo;
import me.angelique.angelNCore.services.ServiceRegistry;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class StockGui {

    public static final String TITLE = TextUtil.color("&8Stock Exchange &7\u2014 &bTrade Shares");
    public static final String TRADE_TITLE = TextUtil.color("&8Stock Exchange &7\u2014 &bPlace Order");
    static final int SIZE = 54;
    static final int TRADE_SIZE = 36;
    private static final int[] ITEM_SLOTS = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
    private static final int[] BUY_AMOUNTS = {1, 5, 10, 50, 100};
    private static final int[] BUY_SLOTS = {10, 11, 12, 13, 14};
    private static final int[] SELL_AMOUNTS = {1, 5, 10, 50, 100};
    private static final int[] SELL_SLOTS = {20, 21, 22, 23, 24};

    static final Map<UUID, List<CompanyInfo>> companyCache = new HashMap<>();
    static final Map<UUID, CompanyInfo> tradeSelection = new HashMap<>();

    private StockGui() {}

    public static void open(Player player, AngelNCore plugin, int page) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.GOLD_INGOT, "&bAngel Stock Exchange",
                "&7Trade company shares in-game",
                "&7Web: &bhttp://127.0.0.1:8080/app/"));

        List<CompanyInfo> companies = ex != null ? ex.listCompanies() : List.of();
        companyCache.put(player.getUniqueId(), companies);

        if (companies.isEmpty()) {
            inv.setItem(22, item(Material.BARRIER, "&cNo companies listed",
                    "&7Use &e/company ipo &7to go public"));
        } else {
            int perPage = ITEM_SLOTS.length;
            int totalPages = Math.max(1, (int) Math.ceil((double) companies.size() / perPage));
            page = Math.max(0, Math.min(page, totalPages - 1));
            int start = page * perPage;
            int end = Math.min(start + perPage, companies.size());

            for (int i = start; i < end; i++) {
                CompanyInfo c = companies.get(i);
                int shares = ex != null ? ex.getHolding(player.getUniqueId(), c.companyId()) : 0;
                inv.setItem(ITEM_SLOTS[i - start], item(Material.PAPER, "&e" + c.name(),
                        "&7Price: &a$" + String.format("%.2f", c.currentPrice()),
                        "&7Shares: &f" + c.totalShares() + " total",
                        "&7You own: &f" + shares + " shares",
                        "&7Volume: &f" + c.volume(),
                        "&7ID: " + c.companyId().substring(0, 8),
                        "",
                        "&eClick to open trade view"));
            }
            if (page > 0) inv.setItem(45, item(Material.ARROW, "&e\u2190 Previous"));
            if (page < totalPages - 1) inv.setItem(53, item(Material.ARROW, "&eNext \u2192"));
        }

        inv.setItem(47, item(Material.BOOK, "&6Portfolio", "&7View your holdings", "", "&eClick to view"));
        inv.setItem(49, item(Material.NAME_TAG, "&bWeb Token", "&7Get auth token for web app", "", "&eClick to generate"));
        inv.setItem(51, item(Material.OAK_DOOR, "&cBack", "&7Return to hub"));

        player.openInventory(inv);
    }

    public static void openTrade(Player player, AngelNCore plugin, CompanyInfo company) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        tradeSelection.put(player.getUniqueId(), company);
        int myShares = ex != null ? ex.getHolding(player.getUniqueId(), company.companyId()) : 0;

        Inventory inv = Bukkit.createInventory(null, TRADE_SIZE, TRADE_TITLE);
        fillBorder36(inv);

        inv.setItem(4, item(Material.GOLD_INGOT, "&b" + company.name(),
                "&7Price: &a$" + String.format("%.2f", company.currentPrice()),
                "&7You own: &f" + myShares + " shares",
                "&7Total: &f" + company.totalShares()));

        // Buy row (slots 10-14)
        inv.setItem(9, item(Material.LIME_STAINED_GLASS_PANE, "&aBUY"));
        for (int i = 0; i < BUY_AMOUNTS.length; i++) {
            double cost = company.currentPrice() * BUY_AMOUNTS[i];
            inv.setItem(BUY_SLOTS[i], item(Material.LIME_TERRACOTTA, "&aBuy " + BUY_AMOUNTS[i] + " shares",
                    "&7Cost: &f$" + String.format("%.2f", cost),
                    "&7@ &f$" + String.format("%.2f", company.currentPrice()) + " each",
                    "",
                    "&eClick to buy"));
        }

        // Sell row (slots 20-24)
        inv.setItem(19, item(Material.RED_STAINED_GLASS_PANE, "&cSELL"));
        for (int i = 0; i < SELL_AMOUNTS.length; i++) {
            double val = company.currentPrice() * SELL_AMOUNTS[i];
            boolean canSell = myShares >= SELL_AMOUNTS[i];
            inv.setItem(SELL_SLOTS[i], item(canSell ? Material.RED_TERRACOTTA : Material.GRAY_TERRACOTTA,
                    (canSell ? "&cSell " : "&7Sell ") + SELL_AMOUNTS[i] + " shares",
                    "&7Value: &f$" + String.format("%.2f", val),
                    "&7@ &f$" + String.format("%.2f", company.currentPrice()) + " each",
                    canSell ? "" : "&cNot enough shares",
                    canSell ? "&eClick to sell" : ""));
        }

        inv.setItem(31, item(Material.OAK_DOOR, "&cBack to list", "&7Return to company list"));

        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    static void fillBorder36(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 36; i++) inv.setItem(i, glass);
    }

    static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(lore).map(TextUtil::color).toList());
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
