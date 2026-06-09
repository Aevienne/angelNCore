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
    static final int SIZE = 54;

    private StockGui() {}

    public static void open(Player player, AngelNCore plugin, int page) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.GOLD_INGOT, "&bAngel Stock Exchange",
                "&7Trade company shares in-game",
                "&7Web: &bhttp://127.0.0.1:8080/app/"));

        List<CompanyInfo> companies = ex != null ? ex.listCompanies() : List.of();
        if (companies.isEmpty()) {
            inv.setItem(22, item(Material.BARRIER, "&cNo companies listed",
                    "&7Use &e/company ipo &7to go public"));
        } else {
            int perPage = 28;
            int totalPages = Math.max(1, (int) Math.ceil((double) companies.size() / perPage));
            page = Math.max(0, Math.min(page, totalPages - 1));
            int start = page * perPage;
            int end = Math.min(start + perPage, companies.size());

            int[] slots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
            for (int i = start; i < end; i++) {
                CompanyInfo c = companies.get(i);
                int shares = ex != null ? ex.getHolding(player.getUniqueId(), c.companyId()) : 0;
                inv.setItem(slots[i - start], item(Material.PAPER, "&e" + c.name(),
                        "&7Price: &a$" + String.format("%.2f", c.currentPrice()),
                        "&7Shares: &f" + c.totalShares() + " total",
                        "&7You own: &f" + shares + " shares",
                        "&7Volume: &f" + c.volume(),
                        "&7ID: " + c.companyId().substring(0, 8),
                        "",
                        "&eClick to trade"));
            }
            if (page > 0) inv.setItem(45, item(Material.ARROW, "&e\u2190 Previous"));
            if (page < totalPages - 1) inv.setItem(53, item(Material.ARROW, "&eNext \u2192"));
        }

        inv.setItem(47, item(Material.BOOK, "&6Portfolio", "&7View your holdings", "", "&eClick to view"));
        inv.setItem(49, item(Material.NAME_TAG, "&bWeb Token", "&7Get auth token for web app", "", "&eClick to generate"));
        inv.setItem(51, item(Material.OAK_DOOR, "&cBack", "&7Return to hub"));

        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);
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
