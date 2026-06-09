package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.tutorial.TutorialSession;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class TutorialGui {

    public static final String TITLE = TextUtil.color("&6Tutorial");
    public static final String SIM_TITLE = TextUtil.color("&6Demo");
    static final int SIZE = 45;

    public static void open(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setActive(true);
        int stepIdx = Math.max(0, s.getStep().ordinal());
        showStep(player, stepIdx);
    }

    public static void resume(Player player) {
        TutorialSession s = TutorialSession.get(player);
        if (!s.isActive()) return;
        int stepIdx = s.getStep().ordinal();
        showStep(player, stepIdx);
    }

    private static void showStep(Player player, int idx) {
        TutorialSession s = TutorialSession.get(player);
        s.setStep(TutorialSession.Step.values()[Math.min(idx, TutorialSession.Step.values().length - 1)]);

        switch (s.getStep()) {
            case INTRO -> showIntro(player);
            case HUB -> showHub(player);
            case SHOP -> showShopDemo(player);
            case CLAIM -> showClaimDemo(player);
            case DUEL -> showDuelDemo(player);
            case BOUNTY -> showBountyDemo(player);
            case STOCK -> showStockDemo(player);
            case BANK -> showBankDemo(player);
            case COSMETICS -> showCosmeticsDemo(player);
            case DONE -> showDone(player);
        }
    }

    // --- Intro ---
    private static void showIntro(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);
        inv.setItem(13, icon(Material.DIAMOND, "&eWelcome!",
                "&7This tutorial will walk you through",
                "&7every system on AngelNetwork.",
                "&7You'll try each feature hands-on",
                "&7in a safe demo environment.",
                "",
                "&aNothing here affects the real world.",
                "",
                "&eClick Next to begin"));
        navButtons(inv, true, false);
        player.openInventory(inv);
    }

    // --- Hub ---
    private static void showHub(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);
        inv.setItem(13, icon(Material.OAK_SIGN, "&eThe Hub",
                "&7Type &f/menu &7in chat to open",
                "&7the main hub at any time.",
                "&7All systems are accessible from there.",
                "",
                "&eClick below to try it now >>"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aOpen Hub Menu",
                "&7Opens the real hub",
                "&7Press &fESC &7to come back here",
                "",
                "&eClick to open"));
        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Shop Demo ---
    private static void showShopDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, SIM_TITLE);
        fillBorder(inv);

        inv.setItem(4, icon(Material.GOLD_NUGGET, "&6Demo Balance: &e$" + String.format("%.2f", s.getDemoBalance()),
                "&7This is practice money",
                "&7It won't affect your real balance"));

        // Fake shop items
        inv.setItem(19, shopItem(Material.BREAD, "&fBread", "$5.00", "$3.50"));
        inv.setItem(20, shopItem(Material.COOKED_BEEF, "&fSteak", "$12.00", "$8.40"));
        inv.setItem(21, shopItem(Material.IRON_INGOT, "&fIron", "$25.00", "$17.50"));
        inv.setItem(22, shopItem(Material.DIAMOND, "&fDiamond", "$100.00", "$70.00"));
        inv.setItem(23, shopItem(Material.OAK_LOG, "&fOak Log", "$3.00", "$2.10"));
        inv.setItem(24, shopItem(Material.ARROW, "&fArrow", "$1.00", "$0.70"));
        inv.setItem(25, shopItem(Material.LEATHER, "&fLeather", "$8.00", "$5.60"));

        inv.setItem(40, icon(Material.KNOWLEDGE_BOOK, "&eBuy any item to continue",
                "&7Click an item to buy it with demo money",
                "&7Right-click to sell if you have one",
                s.isShopCompleted() ? "&aDone! Click Next" : "&7Buy something first"));

        navButtons(inv, true, s.isShopCompleted());
        player.openInventory(inv);
    }

    // --- Claim Demo ---
    private static void showClaimDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.GRASS_BLOCK, "&2Land Claims",
                "&7Stand in any unclaimed chunk",
                "&7and open the claim menu.",
                "&7It costs $" + "100" + " to claim a chunk.",
                "",
                "&eTry it: open the claim menu below"));

        inv.setItem(31, icon(s.isClaimCompleted() ? Material.EMERALD : Material.LIME_TERRACOTTA,
                s.isClaimCompleted() ? "&aClaimed!" : "&aOpen Claim Menu",
                "&7Opens the real claim GUI",
                s.isClaimCompleted() ? "&aDone!" : "&7Claim any chunk, then come back",
                "",
                "&eClick to open"));

        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Duel Demo ---
    private static void showDuelDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        boolean botAlive = s.getDuelBot() != null && s.getDuelBot().isValid();

        inv.setItem(13, icon(Material.DIAMOND_SWORD, "&cDuel Practice",
                botAlive ? "&7Your practice dummy is alive!" : "&7Fight a harmless dummy",
                "&7It barely fights back.",
                "&7Defeat it to complete this step.",
                "",
                s.isDuelCompleted() ? "&aDummy defeated!" : "&eDefeat the dummy to continue"));

        inv.setItem(31, icon(botAlive ? Material.RED_TERRACOTTA : Material.LIME_TERRACOTTA,
                botAlive ? "&cDummy Active" : "&aSpawn Dummy",
                "&7Spawns a baby zombie near you",
                "&7Kill it to complete this step",
                botAlive ? "&7Already spawned" : "",
                "",
                botAlive ? "&eGo fight it!" : "&eClick to spawn"));

        navButtons(inv, true, s.isDuelCompleted());
        player.openInventory(inv);
    }

    // --- Bounty Demo ---
    private static void showBountyDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.WITHER_SKELETON_SKULL, "&4Bounties",
                "&7View wanted players and contracts.",
                "&7The board shows active bounties",
                "&7and open mercenary contracts.",
                "",
                "&eOpen the board below to browse"));

        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aOpen Bounty Board",
                "&7Opens the real wanted board",
                "&7Browse contracts, then come back",
                "",
                "&eClick to open"));

        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Stock Demo ---
    private static void showStockDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.PAPER, "&bStock Exchange",
                "&7Browse listed companies.",
                "&7View price history and place orders.",
                "&7Demo mode: no real money used.",
                "",
                "&eOpen the exchange below"));

        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aOpen Stock Exchange",
                "&7Opens the real stock GUI",
                "&7Browse, then press ESC to return",
                "",
                "&eClick to open"));

        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Bank Demo ---
    private static void showBankDemo(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.GOLD_BLOCK, "&eBank & Loans",
                "&7Take loans against your company.",
                "&7Borrow, repay, and manage debt.",
                "&7Defaulting liquidates your company.",
                "",
                "&eOpen the bank below"));

        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aOpen Bank",
                "&7Opens the real bank GUI",
                "&7Browse loans, then press ESC",
                "",
                "&eClick to open"));

        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Cosmetics ---
    private static void showCosmeticsDemo(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.ELYTRA, "&dCosmetics",
                "&7Equip particle trails and wings.",
                "&7Your choices save between sessions.",
                "",
                "&eTry them below"));

        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aOpen Cosmetics",
                "&7Opens the cosmetics menu",
                "&7Try a trail, then press ESC",
                "",
                "&eClick to open"));

        navButtons(inv, true, true);
        player.openInventory(inv);
    }

    // --- Done ---
    private static void showDone(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(13, icon(Material.EMERALD, "&aTutorial Complete!",
                "&7You've learned all the basics.",
                "&7Type &f/menu &7to open the hub",
                "&7and start your journey.",
                "",
                "&aWelcome to AngelNetwork!",
                "",
                "&7You can replay this anytime",
                "&7with &f/tutorial"));

        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aFinish",
                "&7End the tutorial",
                "",
                "&eClick to finish"));
        inv.setItem(40, icon(Material.BARRIER, "&cExit"));
        player.openInventory(inv);
    }

    // --- Actions ---
    public static void doAction(Player player, int slot) {
        TutorialSession s = TutorialSession.get(player);

        switch (s.getStep()) {
            case INTRO, COSMETICS, DONE -> {}
            case HUB -> { if (slot == 31) { player.closeInventory(); player.performCommand("menu"); } }
            case SHOP -> { if (slot >= 19 && slot <= 25) handleShopBuy(player, slot); }
            case CLAIM -> { if (slot == 31 && !s.isClaimCompleted()) { s.setClaimCompleted(true); player.performCommand("claim"); } }
            case DUEL -> {
                if (slot == 31) {
                    if (s.getDuelBot() == null || !s.getDuelBot().isValid()) {
                        spawnBot(player, s);
                    }
                }
            }
            case BOUNTY -> { if (slot == 31) player.performCommand("wanted"); }
            case STOCK -> { if (slot == 31) player.performCommand("stock"); }
            case BANK -> { if (slot == 31) player.performCommand("bank"); }
        }
    }

    public static void advance(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setStep(TutorialSession.Step.values()[Math.min(s.getStep().ordinal() + 1, TutorialSession.Step.values().length - 1)]);
        showStep(player, s.getStep().ordinal());
    }

    public static void back(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setStep(TutorialSession.Step.values()[Math.max(0, s.getStep().ordinal() - 1)]);
        showStep(player, s.getStep().ordinal());
    }

    public static void exit(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setActive(false);
        if (s.getDuelBot() != null && s.getDuelBot().isValid()) s.getDuelBot().remove();
        player.closeInventory();
    }

    // --- Shop buy handler ---
    private static void handleShopBuy(Player player, int slot) {
        TutorialSession s = TutorialSession.get(player);
        String[] items = {"BREAD", "COOKED_BEEF", "IRON_INGOT", "DIAMOND", "OAK_LOG", "ARROW", "LEATHER"};
        double[] prices = {5.0, 12.0, 25.0, 100.0, 3.0, 1.0, 8.0};
        int idx = slot - 19;
        if (idx < 0 || idx >= items.length) return;

        double price = prices[idx];
        if (s.getDemoBalance() < price) { player.sendMessage(TextUtil.color("&cNot enough demo money.")); return; }

        s.setDemoBalance(s.getDemoBalance() - price);
        Material mat = Material.getMaterial(items[idx]);
        if (mat != null) player.getInventory().addItem(new ItemStack(mat, 1));

        if (!s.isShopCompleted()) {
            s.setShopCompleted(true);
            player.sendMessage(TextUtil.color("&aBought " + items[idx] + "! Step complete. Click Next."));
        }
        showShopDemo(player);
    }

    // --- Bot spawn ---
    private static void spawnBot(Player player, TutorialSession s) {
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        loc.setY(player.getLocation().getY());

        Zombie dummy = (Zombie) player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        dummy.setCustomName(TextUtil.color("&cPractice Dummy"));
        dummy.setCustomNameVisible(true);
        dummy.setBaby();
        dummy.getAttribute(Attribute.MAX_HEALTH).setBaseValue(6);
        dummy.setHealth(6);
        dummy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 4, false, false));
        dummy.setTarget(player);
        s.setDuelBot(dummy);
        s.setDuelCompleted(false);

        player.sendMessage(TextUtil.color("&aA dummy spawned! Defeat it, then click Next."));
        showDuelDemo(player);
    }

    // --- Nav buttons ---
    static void navButtons(Inventory inv, boolean hasNext, boolean nextEnabled) {
        Material nextMat = nextEnabled ? Material.LIME_TERRACOTTA : Material.GRAY_TERRACOTTA;
        inv.setItem(36, icon(Material.ARROW, "&ePrevious"));
        inv.setItem(44, icon(nextMat, nextEnabled ? "&aNext" : "&7Complete step first"));
        inv.setItem(40, icon(Material.BARRIER, "&cExit Tutorial"));
    }

    // --- Shop item ---
    static ItemStack shopItem(Material mat, String name, String buy, String sell) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(List.of(
                    TextUtil.color("&aBuy: &f$" + buy + " each"),
                    TextUtil.color("&eSell: &f$" + sell + " each"),
                    "",
                    TextUtil.color("&eClick to buy (demo)")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- Utilities ---
    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    static ItemStack icon(Material mat, String name, String... lore) {
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
