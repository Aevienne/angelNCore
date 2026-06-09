package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class AngelHubGui {

    public static final String TITLE = TextUtil.color("&8Angel Network &7\u2014 &6Main Menu");
    public static final int SIZE = 54;

    private AngelHubGui() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);

        // Row 1: Economic systems (slots 19-25)
        inv.setItem(19, icon(Material.EMERALD, "&aServer Shop",
                "&7Buy & sell items at dynamic prices",
                "&7Prices shift with supply & demand",
                "",
                "&eClick to open"));
        inv.setItem(20, icon(Material.BOOK, "&6Market Prices",
                "&7View cross-venue market data",
                "&7Compare prices across regions",
                "",
                "&eClick to view"));
        inv.setItem(21, icon(Material.GOLD_BLOCK, "&eBank & Loans",
                "&7Take loans against your assets",
                "&7Repay with interest over time",
                "",
                "&eClick to open"));
        inv.setItem(22, icon(Material.PAPER, "&bStock Exchange",
                "&7Trade company shares",
                "&7IPO your company to go public",
                "",
                "&eClick to browse"));
        inv.setItem(23, icon(Material.COMPASS, "&dTrade Routes",
                "&7Create & manage trade routes",
                "&7Set up shops along caravan paths",
                "",
                "&eClick to manage"));
        inv.setItem(24, icon(Material.CRAFTING_TABLE, "&5Companies",
                "&7Register a company",
                "&7Create products & factories",
                "",
                "&eClick to manage"));
        inv.setItem(25, icon(Material.GOLD_INGOT, "&6Auction House",
                "&7Browse & bid on player auctions",
                "&7List your items for bidding",
                "",
                "&eClick to open"));

        // Row 2: Combat & Social (slots 28-34)
        inv.setItem(28, icon(Material.DIAMOND_SWORD, "&cDuels & Honor",
                "&7Challenge other players to duels",
                "&7Wager money & climb the leaderboard",
                "",
                "&eClick to open"));
        inv.setItem(29, icon(Material.WITHER_SKELETON_SKULL, "&4Bounties",
                "&7Post bounties on wanted players",
                "&7Accept mercenary contracts",
                "",
                "&eClick to open"));
        inv.setItem(30, icon(Material.ELYTRA, "&dCosmetics",
                "&7Equip particle trails & wings",
                "&7Set recall points & teleport effects",
                "",
                "&eClick to open"));
        inv.setItem(31, icon(Material.FURNACE, "&7Factories",
                "&7Place and manage factories",
                "&7Automate item production",
                "",
                "&eClick to manage"));
        inv.setItem(32, icon(Material.CLOCK, "&3Seasons",
                "&7View current season & weather",
                "&7Blood Moon events & bonuses",
                "",
                "&eClick to view"));
        inv.setItem(33, icon(Material.GOLDEN_APPLE, "&cNutrition",
                "&7Track your diet & nutrition stats",
                "&7Eat varied foods for bonuses",
                "",
                "&eClick to view"));
        inv.setItem(34, icon(Material.OAK_FENCE_GATE, "&2Land Claims",
                "&7Claim & manage land chunks",
                "&7Territory control & regions",
                "",
                "&eClick to manage"));

        // Navigation bar (row 5)
        inv.setItem(45, icon(Material.OAK_SIGN, "&eWelcome, &f" + player.getName(),
                "&7Angel Network Economy",
                "&7Phase 1 \u2014 Survival"));
        inv.setItem(49, icon(Material.BARRIER, "&cClose", "&7Click to close menu"));
        inv.setItem(53, icon(Material.KNOWLEDGE_BOOK, "&bAngel Network",
                "&7v1.0 \u2014 Player-driven economy",
                "&7Build, trade, conquer"));

        player.openInventory(inv);
    }

    static ItemStack icon(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(loreLines).map(TextUtil::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    static ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }
}
