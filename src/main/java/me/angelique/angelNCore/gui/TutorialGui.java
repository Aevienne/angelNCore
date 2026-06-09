package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
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
    static final int SIZE = 45;
    static final Map<UUID, Integer> tutorialStep = new HashMap<>();
    private static final Map<UUID, Zombie> tutorialBots = new HashMap<>();

    static final String[][] STEPS = {
        // Each step: [icon_material, title, ...lore, action_type]
        // action_type: "info", "open_shop", "open_claim", "open_duel", "open_bounty", "open_stock", "done"
        {"&6DIAMOND", "&eWelcome", "&7Learn how AngelNetwork works", "&7through hands-on demos.", "", "&eClick Next to start!", "info"},
        {"&6OAK_SIGN", "&eThe Hub", "&7Everything starts at &f/menu", "&7It's your control panel.", "", "&eClick to open the hub", "open"},
        {"&aEMERALD", "&aShop Demo", "&7Lets practice buying and selling.", "&7You'll have demo credits.", "", "&eClick to open practice shop", "shop"},
        {"&2OAK_FENCE_GATE", "&2Land Claims", "&7Stand where you want to claim.", "&7Claiming costs $" + "100" + " per chunk.", "", "&eClick to open claim menu", "claim"},
        {"&cDIAMOND_SWORD", "&cDuel Demo", "&7Fight a practice dummy!", "&7The dummy wont fight back.", "&7Defeat it to earn an Honor Token.", "", "&eClick to spawn a dummy", "duel"},
        {"&4WITHER_SKELETON_SKULL", "&4Bounty Demo", "&7View the wanted board.", "&7Accept a contract to practice.", "", "&eClick to open bounty board", "bounty"},
        {"&bPAPER", "&bStock Demo", "&7Browse companies and trade shares.", "&7Practice placing buy orders.", "", "&eClick to open stock exchange", "stock"},
        {"&eGOLD_BLOCK", "&eBank Demo", "&7View loans and borrowing.", "&7Practice repaying a loan.", "", "&eClick to open bank", "bank"},
        {"&dELYTRA", "&dCosmetics", "&7Customize trails and wings.", "&7Try them out in the menu.", "", "&eClick to open cosmetics", "cosmetics"},
        {"&6KNOWLEDGE_BOOK", "&6Done!", "&7You're ready to play.", "&7Use &f/menu &7to get started.", "", "&aWelcome to AngelNetwork!", "done"}
    };

    private TutorialGui() {}

    public static void open(Player player, int step) {
        if (step < 0) step = 0;
        if (step >= STEPS.length) step = STEPS.length - 1;
        tutorialStep.put(player.getUniqueId(), step);

        String[] data = STEPS[step];
        Material icon = Material.matchMaterial(data[0].substring(2));
        String title = data[1];
        String actionType = data[data.length - 1];
        String[] lore = Arrays.copyOfRange(data, 2, data.length - 1);

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.KNOWLEDGE_BOOK, "&6Step " + (step + 1) + " of " + STEPS.length));
        inv.setItem(22, item(icon != null ? icon : Material.BOOK, title, lore));

        // Action button
        Material actionIcon = Material.LIME_TERRACOTTA;
        String actionLabel = switch (actionType) {
            case "shop" -> { actionIcon = Material.EMERALD; yield "&aOpen Practice Shop"; }
            case "claim" -> { actionIcon = Material.OAK_FENCE_GATE; yield "&aOpen Claim Menu"; }
            case "duel" -> { actionIcon = Material.DIAMOND_SWORD; yield "&aSpawn Practice Dummy"; }
            case "bounty" -> { actionIcon = Material.WITHER_SKELETON_SKULL; yield "&aOpen Bounty Board"; }
            case "stock" -> { actionIcon = Material.PAPER; yield "&aOpen Stock Exchange"; }
            case "bank" -> { actionIcon = Material.GOLD_BLOCK; yield "&aOpen Bank"; }
            case "cosmetics" -> { actionIcon = Material.ELYTRA; yield "&aOpen Cosmetics"; }
            case "open" -> { actionIcon = Material.OAK_SIGN; yield "&aOpen Hub Menu"; }
            case "done" -> { actionIcon = Material.EMERALD; yield "&aFinish Tutorial"; }
            default -> { actionIcon = Material.LIME_TERRACOTTA; yield "&aContinue"; }
        };
        inv.setItem(31, item(actionIcon, actionLabel, "&7Click to do this step", "", "&eThen click Next"));

        if (step > 0) inv.setItem(36, item(Material.ARROW, "&ePrevious"));
        if (step < STEPS.length - 1) inv.setItem(44, item(Material.ARROW, "&eNext"));
        inv.setItem(40, item(Material.BARRIER, "&cExit Tutorial"));

        player.openInventory(inv);
    }

    public static void doAction(Player player, String actionType) {
        plugin: switch (actionType) {
            case "shop" -> {
                if (AngelNCore.getInstance() != null) {
                    ShopGui.open(player, AngelNCore.getInstance(), 0);
                }
            }
            case "claim" -> {
                if (AngelNCore.getInstance() != null) {
                    ClaimGui.open(player, AngelNCore.getInstance());
                }
            }
            case "duel" -> {
                spawnDummy(player);
                break plugin;
            }
            case "bounty" -> {
                player.closeInventory();
                player.performCommand("wanted");
            }
            case "stock" -> {
                if (AngelNCore.getInstance() != null) {
                    StockGui.open(player, AngelNCore.getInstance(), 0);
                }
            }
            case "bank" -> {
                if (AngelNCore.getInstance() != null) {
                    BankGui.open(player, AngelNCore.getInstance());
                }
            }
            case "cosmetics" -> {
                player.performCommand("cosmetics");
            }
            case "open" -> {
                player.closeInventory();
                player.performCommand("menu");
            }
        }
    }

    private static void spawnDummy(Player player) {
        // Remove old dummy
        Zombie old = tutorialBots.remove(player.getUniqueId());
        if (old != null && old.isValid()) old.remove();

        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        loc.setY(player.getLocation().getY());

        Zombie dummy = (Zombie) player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        dummy.setCustomName(TextUtil.color("&cPractice Dummy"));
        dummy.setCustomNameVisible(true);
        dummy.setBaby();
        dummy.getAttribute(Attribute.MAX_HEALTH).setBaseValue(4);
        dummy.setHealth(4);
        dummy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 4, false, false));
        dummy.setTarget(player);
        tutorialBots.put(player.getUniqueId(), dummy);

        player.sendMessage(TextUtil.color("&aA practice dummy has appeared! Defeat it to complete the duel demo."));
    }

    public static void cleanupDummy(Player player) {
        Zombie dummy = tutorialBots.remove(player.getUniqueId());
        if (dummy != null && dummy.isValid()) dummy.remove();
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
