package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.StockExchangeService.*;
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

    public static final String TITLE = TextUtil.color("&bTrade Shares");
    public static final String TRADE_TITLE = TextUtil.color("&bPlace Order");
    static final int SIZE = 54;
    private static final int[] ITEM_SLOTS = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};
    private static final int[] BUY_AMOUNTS = {1, 5, 10, 50, 100};
    private static final int[] SELL_AMOUNTS = {1, 5, 10, 50, 100};

    static final Map<UUID, List<CompanyInfo>> companyCache = new HashMap<>();
    static final Map<UUID, CompanyInfo> tradeSelection = new HashMap<>();

    private StockGui() {}

    public static void open(Player player, AngelNCore plugin, int page) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        double totalValue = 0;
        if (ex != null) {
            for (CompanyInfo c : ex.listCompanies()) {
                totalValue += ex.getHolding(player.getUniqueId(), c.companyId()) * c.currentPrice();
            }
        }

        inv.setItem(4, item(Material.GOLD_NUGGET, "&6Portfolio: &e$" + String.format("%.2f", totalValue),
                totalValue > 0 ? "&7Click a company to trade" : "&7Use /company ipo to list a company"));

        List<CompanyInfo> companies = ex != null ? ex.listCompanies() : List.of();
        companyCache.put(player.getUniqueId(), companies);

        if (companies.isEmpty()) {
            inv.setItem(22, item(Material.BARRIER, "&cNo companies listed", "&7Use &e/company ipo &7to go public"));
        } else {
            int perPage = ITEM_SLOTS.length;
            int totalPages = Math.max(1, (int) Math.ceil((double) companies.size() / perPage));
            page = Math.max(0, Math.min(page, totalPages - 1));
            int start = page * perPage;
            int end = Math.min(start + perPage, companies.size());

            for (int i = start; i < end; i++) {
                CompanyInfo c = companies.get(i);
                int myShares = ex != null ? ex.getHolding(player.getUniqueId(), c.companyId()) : 0;
                double myValue = myShares * c.currentPrice();
                List<PriceCandle> history = ex != null ? ex.getPriceHistory(c.companyId()) : List.of();
                String trend = "";
                if (history.size() >= 2) {
                    double prev = history.get(history.size() - 2).close();
                    double curr = c.currentPrice();
                    double pct = ((curr - prev) / prev) * 100;
                    trend = pct >= 0 ? " &a+" + String.format("%.1f", pct) + "%" : " &c" + String.format("%.1f", pct) + "%";
                }

                List<String> lore = new ArrayList<>(List.of(
                        "&7Price: &a$" + String.format("%.2f", c.currentPrice()) + trend,
                        "&7Volume: &f" + c.volume() + " trades"
                ));
                if (myShares > 0) lore.add("&7You own: &f" + myShares + " shares (&a$" + String.format("%.2f", myValue) + ")");
                lore.add("");
                lore.add("&eClick to view details and trade");

                inv.setItem(ITEM_SLOTS[i - start], item(Material.PAPER, "&e" + c.name(),
                        lore.toArray(new String[0])));
            }
            if (page > 0) inv.setItem(45, item(Material.ARROW, "&ePrevious"));
            if (page < totalPages - 1) inv.setItem(53, item(Material.ARROW, "&eNext"));
        }

        inv.setItem(47, item(Material.BOOK, "&6My Portfolio", "&7View your holdings"));
        inv.setItem(49, item(Material.NAME_TAG, "&bWeb App", "&7Get access token for website", "", "&eClick to generate"));
        inv.setItem(51, item(Material.OAK_DOOR, "&cBack"));

        player.openInventory(inv);
    }

    public static void openTrade(Player player, AngelNCore plugin, CompanyInfo company) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        tradeSelection.put(player.getUniqueId(), company);
        int myShares = ex != null ? ex.getHolding(player.getUniqueId(), company.companyId()) : 0;
        double myValue = myShares * company.currentPrice();

        Inventory inv = Bukkit.createInventory(null, 54, TRADE_TITLE);
        fillBorder(inv);

        // Header: company info
        List<PriceCandle> history = ex != null ? ex.getPriceHistory(company.companyId()) : List.of();
        String trend = "";
        if (history.size() >= 2) {
            double prev = history.get(history.size() - 2).close();
            double pct = ((company.currentPrice() - prev) / prev) * 100;
            trend = pct >= 0 ? " &a+" + String.format("%.1f", pct) + "%" : " &c" + String.format("%.1f", pct) + "%";
        }

        inv.setItem(4, item(Material.GOLD_INGOT, "&b" + company.name() + trend,
                "&7Price: &a$" + String.format("%.2f", company.currentPrice()),
                "&7Shares: &f" + company.totalShares() + " total",
                "&7Volume: &f" + company.volume(),
                myShares > 0 ? "&7You own: &f" + myShares + " shares (&a$" + String.format("%.2f", myValue) + ")" : "&7You own: &f0 shares"));

        // Price history bar (slots 10-16 = 7 candles)
        int toShow = Math.min(7, history.size());
        for (int i = 0; i < toShow; i++) {
            int idx = history.size() - toShow + i;
            PriceCandle candle = history.get(idx);
            boolean up = candle.close() >= candle.open();
            Material mat = up ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            inv.setItem(10 + i, item(mat, up ? "&a$" + String.format("%.2f", candle.close()) : "&c$" + String.format("%.2f", candle.close()),
                    "&7O: $" + String.format("%.2f", candle.open()),
                    "&7H: $" + String.format("%.2f", candle.high()),
                    "&7L: $" + String.format("%.2f", candle.low()),
                    "&7V: " + candle.volume()));
        }

        // Buy row (slots 20-24)
        for (int i = 0; i < BUY_AMOUNTS.length; i++) {
            double cost = company.currentPrice() * BUY_AMOUNTS[i];
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            boolean canAfford = balance >= cost;
            inv.setItem(20 + i, item(canAfford ? Material.LIME_TERRACOTTA : Material.GRAY_TERRACOTTA,
                    "&aBuy " + BUY_AMOUNTS[i],
                    "&7$" + String.format("%.2f", cost),
                    canAfford ? "&eClick to buy" : "&cCannot afford"));
        }

        // Sell row (slots 29-33)
        for (int i = 0; i < SELL_AMOUNTS.length; i++) {
            boolean canSell = myShares >= SELL_AMOUNTS[i];
            double val = company.currentPrice() * SELL_AMOUNTS[i];
            inv.setItem(29 + i, item(canSell ? Material.RED_TERRACOTTA : Material.GRAY_TERRACOTTA,
                    "&cSell " + SELL_AMOUNTS[i],
                    "&7$" + String.format("%.2f", val),
                    canSell ? "&eClick to sell" : "&cNot enough shares"));
        }

        // Order book preview (slots 37-43)
        List<OrderInfo> orders = ex != null ? ex.getOrderBook(company.companyId()) : List.of();
        int orderSlot = 37;
        for (OrderInfo o : orders) {
            if (orderSlot > 43) break;
            boolean isBuy = "buy".equals(o.type());
            inv.setItem(orderSlot++, item(isBuy ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                    (isBuy ? "&aBUY " : "&cSELL ") + o.shares() + " @ $" + String.format("%.2f", o.price()),
                    "&7" + o.status()));
        }

        inv.setItem(49, item(Material.OAK_DOOR, "&cBack to list"));
        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
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
