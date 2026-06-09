package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class TutorialGui {

    public static final String TITLE = TextUtil.color("&6Tutorial");
    static final int SIZE = 45;

    private static final String[][] STEPS = {
        { // 0: Welcome
            "&6DIAMOND", "&eWelcome to AngelNetwork!",
            "&7A player-driven economy where",
            "&7everything you do matters.",
            "&7This tutorial will walk you through",
            "&7all the systems available to you.",
            "",
            "&eClick Next to begin!"
        },
        { // 1: Main Menu
            "&6OAK_SIGN", "&eThe Hub Menu",
            "&7Use &f/menu &7to open the main hub.",
            "&7From there you can access everything:",
            "&7shop, bank, stock exchange, duels,",
            "&7bounties, companies, and more.",
            "&7It's your one-stop control panel.",
            "",
            "&eTry it: type &f/menu &eright now!"
        },
        { // 2: Shop
            "&aEMERALD", "&aServer Shop",
            "&7Buy and sell items at dynamic prices.",
            "&7Prices shift with supply and demand.",
            "&7Click an item, choose an amount,",
            "&7and trade instantly.",
            "",
            "&eOpen with &f/shop &eor via the hub"
        },
        { // 3: Land Claims
            "&2OAK_FENCE_GATE", "&2Land Claims",
            "&7Claim chunks of land as your own.",
            "&7Stand in a chunk and open &f/claim",
            "&7to claim it for a small fee.",
            "&7Crossing into claimed territory",
            "&7shows the owner's name on screen.",
            "",
            "&eEach player gets up to 10 claims"
        },
        { // 4: Companies
            "&5CRAFTING_TABLE", "&5Companies",
            "&7Form a company with &f/company create",
            "&7Create products with custom effects,",
            "&7manage factories, hold patents,",
            "&7and go public via IPO.",
            "",
            "&eCompanies can own land and hire workers"
        },
        { // 5: Stock Exchange
            "&bPAPER", "&bStock Exchange",
            "&7Buy and sell shares of companies.",
            "&7When a company IPOs, its shares",
            "&7become available for public trading.",
            "&7Track prices, place orders, and",
            "&7build your investment portfolio.",
            "",
            "&eOpen with &f/stock &eor via hub"
        },
        { // 6: Bank & Loans
            "&eGOLD_BLOCK", "&eBank & Loans",
            "&7Borrow money against your company",
            "&7using &f/bank",
            "&7Loans accrue interest over time.",
            "&7Defaulting liquidates your company.",
            "",
            "&eMax 3 active loans at a time"
        },
        { // 7: Duels
            "&cDIAMOND_SWORD", "&cHonor Duels",
            "&7Challenge players to 1v1 combat.",
            "&7Open &f/challenge &7to see the arena.",
            "&7Select an opponent, they accept,",
            "&7and the duel begins.",
            "&7Winners earn Honor Tokens.",
            "",
            "&eNo items lost during duels"
        },
        { // 8: Bounties
            "&4WITHER_SKELETON_SKULL", "&4Bounties & Contracts",
            "&7View wanted players and their bounties",
            "&7with &f/wanted",
            "&7Post contracts for mercenary work:",
            "&7player kills, sabotage, escorts.",
            "&7Accept contracts and earn rewards.",
            "",
            "&eHigher stars = bigger bounty"
        },
        { // 9: Trade Routes
            "&dCOMPASS", "&dTrade Routes",
            "&7Connect waystones to create trade paths",
            "&7using &f/route",
            "&7Routes boost shop sales and travel.",
            "&7Routes can be insured against attack.",
            "&7Tiers upgrade with usage.",
            "",
            "&eRight-click waystones with a Route Deed"
        },
        { // 10: Factories
            "&7FURNACE", "&7Factories",
            "&7Place factories to automate production",
            "&7with &f/factory place",
            "&7Feed them fuel and inputs,",
            "&7collect outputs when done.",
            "&7Sell products through your shop.",
            "",
            "&eCompany members can manage factories"
        },
        { // 11: Cosmetics
            "&dELYTRA", "&dCosmetics",
            "&7Customize your appearance with",
            "&7particle trails, wings, and recalls.",
            "&7Open &f/cosmetics &7to equip them.",
            "&7Selections save between sessions.",
            "",
            "&eTeleport with style using recall"
        },
        { // 12: Seasons & Nutrition
            "&3CLOCK", "&3Seasons & Nutrition",
            "&7The world changes with the seasons.",
            "&7View status with &f/season",
            "&7Track your diet with &f/sustenance",
            "&7Eat varied foods for buffs.",
            "&7Blood moons empower monsters.",
            "",
            "&eSeasons affect crop growth and spawns"
        },
        { // 13: Done
            "&6KNOWLEDGE_BOOK", "&6You're Ready!",
            "&7You now know all the systems.",
            "&7Press &f/menu &7to open the hub",
            "&7and start your journey.",
            "",
            "&aGood luck, and welcome to",
            "&aAngelNetwork!"
        }
    };

    static final Map<UUID, Integer> tutorialStep = new HashMap<>();

    private TutorialGui() {}

    public static void open(Player player, int step) {
        if (step < 0) step = 0;
        if (step >= STEPS.length) step = STEPS.length - 1;
        tutorialStep.put(player.getUniqueId(), step);

        String[] data = STEPS[step];
        Material icon = Material.matchMaterial(data[0].substring(2));
        String title = data[1];
        String[] lore = Arrays.copyOfRange(data, 2, data.length);

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        // Step counter
        inv.setItem(4, item(Material.KNOWLEDGE_BOOK, "&6Step " + (step + 1) + " of " + STEPS.length,
                "&7" + title));

        // Main icon
        inv.setItem(22, item(icon != null ? icon : Material.BOOK, title, lore));

        // Navigation
        if (step > 0) inv.setItem(36, item(Material.ARROW, "&ePrevious"));
        if (step < STEPS.length - 1) inv.setItem(44, item(Material.ARROW, "&eNext"));
        inv.setItem(40, item(Material.BARRIER, "&cExit Tutorial"));

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
