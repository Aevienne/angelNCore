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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class TutorialGui {

    public static final String TITLE = TextUtil.color("&6Tutorial");
    public static final String SIM_TITLE = TextUtil.color("&6Demo Shop");
    static final int SIZE = 45;

    public static void open(Player player) {
        TutorialSession s = TutorialSession.get(player);
        if (s.getStep() == TutorialSession.Step.DONE) s.reset();
        s.setActive(true);
        showStep(player);
    }

    private static void showStep(Player player) {
        TutorialSession s = TutorialSession.get(player);
        switch (s.getStep()) {
            case INTRO -> showIntro(player);
            case HUB -> showHub(player);
            case SHOP -> showShop(player);
            case CLAIM -> showClaim(player);
            case DUEL -> showDuel(player);
            case BOUNTY -> showBounty(player);
            case STOCK -> showStock(player);
            case BANK -> showBank(player);
            case FACTORY -> showFactory(player);
            case COSMETICS -> showCosmetics(player);
            case DONE -> showDone(player);
        }
    }

    // --- Steps ---
    private static void showIntro(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.DIAMOND, "&eWelcome!",
                "&7Learn how AngelNetwork works",
                "&7through hands-on demos.",
                "",
                "&aNothing here affects the real world.",
                "",
                "&eClick Next to begin"));
        nav(inv, false, true);
        player.openInventory(inv);
    }

    private static void showHub(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.OAK_SIGN, "&eHub Menu",
                "&7The hub is your control panel.",
                "&7All systems are one click away.",
                "",
                "&eTry it now:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Hub Menu",
                "&7Opens the real hub",
                "&7Press ESC to return here",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showShop(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = Bukkit.createInventory(null, SIZE, SIM_TITLE);
        fill(inv);

        inv.setItem(4, icon(Material.GOLD_NUGGET, "&6Demo Balance: &e$" + String.format("%.2f", s.getDemoBalance()),
                "&7This is practice money"));

        inv.setItem(19, shopItem(Material.BREAD, "&fBread", "$5"));
        inv.setItem(20, shopItem(Material.COOKED_BEEF, "&fSteak", "$12"));
        inv.setItem(21, shopItem(Material.IRON_INGOT, "&fIron", "$25"));
        inv.setItem(22, shopItem(Material.DIAMOND, "&fDiamond", "$100"));
        inv.setItem(23, shopItem(Material.OAK_LOG, "&fOak Log", "$3"));
        inv.setItem(24, shopItem(Material.ARROW, "&fArrow", "$1"));
        inv.setItem(25, shopItem(Material.LEATHER, "&fLeather", "$8"));

        inv.setItem(40, icon(Material.KNOWLEDGE_BOOK, "&eBuy 1 item to continue",
                s.isShopDone() ? "&aDone! Click Next" : "&7Click any item to buy it"));
        nav(inv, true, s.isShopDone());
        player.openInventory(inv);
    }

    private static void showClaim(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.GRASS_BLOCK, "&2Land Claims",
                "&7Claim chunks of land as your own.",
                "&7Stand in an unclaimed chunk",
                "&7and claim it for $" + "100" + ".",
                "",
                "&eTry it:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Claim Menu",
                "&7Opens the claim GUI",
                "&7Claim any chunk, press ESC",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showDuel(Player player) {
        TutorialSession s = TutorialSession.get(player);
        Inventory inv = baseInv();
        boolean botAlive = s.getDuelBot() != null && s.getDuelBot().isValid();

        inv.setItem(13, icon(Material.DIAMOND_SWORD, "&cDuel Demo",
                botAlive ? "&7Dummy is alive! Fight it!" : "&7Spawn a practice dummy",
                "&7It's slow and has low health.",
                "",
                s.isDuelDone() ? "&aDummy defeated!" : "&eKill it to continue"));
        inv.setItem(31, icon(botAlive ? Material.RED_TERRACOTTA : Material.LIME_TERRACOTTA,
                botAlive ? "&cDummy fighting" : "&aSpawn Dummy",
                botAlive ? "&7Kill it, then click Next" : "&7Spawns near you",
                "",
                botAlive ? "&eGo fight!" : "&eClick to spawn"));
        nav(inv, true, s.isDuelDone());
        player.openInventory(inv);
    }

    private static void showBounty(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.WITHER_SKELETON_SKULL, "&4Bounties",
                "&7View wanted players & contracts.",
                "&7Post bounties, accept work.",
                "",
                "&eTry it:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Bounty Board",
                "&7Opens the bounty GUI",
                "&7Press ESC to return",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showStock(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.PAPER, "&bStock Exchange",
                "&7Trade company shares.",
                "&7Browse listings, place orders.",
                "",
                "&eTry it:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Stock Exchange",
                "&7Opens the stock GUI",
                "&7Press ESC to return",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showBank(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.GOLD_BLOCK, "&eBank",
                "&7Borrow, repay, manage loans.",
                "&7Loans require company collateral.",
                "",
                "&eTry it:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Bank",
                "&7Opens the bank GUI",
                "&7Press ESC to return",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showFactory(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.FURNACE, "&7Factories",
                "&7Automate item production.",
                "&7Place factories, feed fuel,",
                "&7and collect outputs.",
                "",
                "&e/factory to manage yours"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showCosmetics(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.ELYTRA, "&dCosmetics",
                "&7Particle trails, wings, recalls.",
                "&7Your choices save between sessions.",
                "",
                "&eTry it:"));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aDemo: Cosmetics",
                "&7Opens the cosmetics menu",
                "&7Press ESC to return",
                "",
                "&eClick to open"));
        nav(inv, true, true);
        player.openInventory(inv);
    }

    private static void showDone(Player player) {
        Inventory inv = baseInv();
        inv.setItem(13, icon(Material.EMERALD, "&aTutorial Complete!",
                "&7You're ready to play.",
                "&7Use &f/menu &7to open the hub.",
                "&7Use &f/tutorial &7to replay."));
        inv.setItem(31, icon(Material.LIME_TERRACOTTA, "&aFinish", "", "&eClick to exit"));
        inv.setItem(40, icon(Material.BARRIER, "&cExit"));
        player.openInventory(inv);
    }

    // --- Actions ---
    public static void doAction(Player player, int slot) {
        TutorialSession s = TutorialSession.get(player);
        var step = s.getStep();

        if (slot == 31) {
            s.setPendingSubGui(true);
            player.closeInventory();
            switch (step) {
                case HUB -> scheduleCommand(player, "menu");
                case CLAIM -> scheduleCommand(player, "claim");
                case BOUNTY -> scheduleCommand(player, "wanted");
                case STOCK -> scheduleCommand(player, "stock");
                case BANK -> scheduleCommand(player, "bank");
                case COSMETICS -> scheduleCommand(player, "cosmetics");
                case DUEL -> spawnBot(player, s);
                case DONE -> { s.setActive(false); player.closeInventory(); }
                default -> {}
            }
        }

        if (step == TutorialSession.Step.SHOP && slot >= 19 && slot <= 25) {
            handleShopBuy(player, slot);
        }
    }

    private static void scheduleCommand(Player player, String cmd) {
        new BukkitRunnable() { @Override public void run() { player.performCommand(cmd); } }
                .runTaskLater(AngelNCore.getInstance(), 2L);
    }

    // --- Shop ---
    private static void handleShopBuy(Player player, int slot) {
        TutorialSession s = TutorialSession.get(player);
        if (s.isShopDone()) { player.sendMessage(TextUtil.color("&cShop demo done. Click Next.")); return; }
        String[] items = {"BREAD","COOKED_BEEF","IRON_INGOT","DIAMOND","OAK_LOG","ARROW","LEATHER"};
        double[] prices = {5,12,25,100,3,1,8};
        int idx = slot - 19;
        if (idx < 0 || idx >= items.length) return;
        if (s.getDemoBalance() < prices[idx]) { player.sendMessage(TextUtil.color("&cNot enough demo money.")); return; }
        s.setDemoBalance(s.getDemoBalance() - prices[idx]);
        Material mat = Material.getMaterial(items[idx]);
        if (mat != null) player.getInventory().addItem(new ItemStack(mat, 1));
        s.setShopDone(true);
        player.sendMessage(TextUtil.color("&aBought " + items[idx] + "! Step done. Click Next."));
        showShop(player);
    }

    // --- Bot ---
    private static void spawnBot(Player player, TutorialSession s) {
        if (s.getDuelBot() != null && s.getDuelBot().isValid()) { showDuel(player); return; }
        Location loc = player.getLocation().clone();
        // Find safe ground
        loc.setY(loc.getY() + 1);
        while (loc.getBlock().getType().isSolid() && loc.getY() < 320) loc.setY(loc.getY() + 1);
        while (!loc.getBlock().getType().isSolid() && loc.getY() > -60) loc.setY(loc.getY() - 1);
        loc.setY(loc.getY() + 1);

        Zombie bot = (Zombie) player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        bot.setCustomName(TextUtil.color("&cPractice Dummy"));
        bot.setCustomNameVisible(true);
        bot.setBaby();
        bot.getAttribute(Attribute.MAX_HEALTH).setBaseValue(6);
        bot.setHealth(6);
        bot.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 4, false, false));
        bot.setTarget(player);
        s.setDuelBot(bot);
        s.setDuelDone(false);
        player.sendMessage(TextUtil.color("&aDummy spawned nearby! Kill it, then click Next."));
        showDuel(player);
    }

    // --- Navigation ---
    public static void advance(Player player) {
        TutorialSession s = TutorialSession.get(player);
        var values = TutorialSession.Step.values();
        s.setStep(values[Math.min(s.getStep().ordinal() + 1, values.length - 1)]);
        showStep(player);
    }

    public static void back(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setStep(TutorialSession.Step.values()[Math.max(0, s.getStep().ordinal() - 1)]);
        showStep(player);
    }

    public static void exit(Player player) {
        TutorialSession s = TutorialSession.get(player);
        s.setActive(false);
        if (s.getDuelBot() != null && s.getDuelBot().isValid()) s.getDuelBot().remove();
        player.closeInventory();
    }

    // --- Utility ---
    private static Inventory baseInv() { Inventory inv = Bukkit.createInventory(null, SIZE, TITLE); fill(inv); return inv; }
    static void fill(Inventory inv) { ItemStack g = pane(Material.BLACK_STAINED_GLASS_PANE); for (int i=0;i<inv.getSize();i++) inv.setItem(i,g); }
    static void nav(Inventory inv, boolean nextEnabled, boolean hasNext) {
        inv.setItem(36, icon(Material.ARROW, "&ePrevious"));
        inv.setItem(44, icon(nextEnabled ? Material.LIME_TERRACOTTA : Material.GRAY_TERRACOTTA, nextEnabled ? "&aNext" : "&7Complete step first"));
        inv.setItem(40, icon(Material.BARRIER, "&cExit Tutorial"));
    }
    static ItemStack shopItem(Material mat, String name, String price) {
        ItemStack item = new ItemStack(mat); ItemMeta m = item.getItemMeta();
        if (m != null) { m.setDisplayName(TextUtil.color(name)); m.setLore(List.of(TextUtil.color("&a$" + price + " each"), "", TextUtil.color("&eClick to buy (demo)"))); item.setItemMeta(m); }
        return item;
    }
    static ItemStack icon(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat); ItemMeta m = item.getItemMeta();
        if (m != null) { m.setDisplayName(TextUtil.color(name)); m.setLore(Arrays.stream(lore).map(TextUtil::color).toList()); item.setItemMeta(m); }
        return item;
    }
    static ItemStack pane(Material mat) { ItemStack item = new ItemStack(mat); ItemMeta m = item.getItemMeta(); if (m != null) { m.setDisplayName(" "); item.setItemMeta(m); } return item; }
}
