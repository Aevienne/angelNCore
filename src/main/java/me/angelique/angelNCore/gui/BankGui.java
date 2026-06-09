package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.ServiceRegistry;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class BankGui {

    public static final String TITLE = TextUtil.color("&8Bank &7\u2014 &eLoans & Repay");
    static final int SIZE = 45;
    private static final int[] LOAN_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] BORROW_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final double[] BORROW_AMOUNTS = {1000, 5000, 10000, 30000, 50000, 100000, 250000};
    private static final int[] BORROW_TERMS = {5, 10, 15, 30, 30, 60, 90};

    static final Map<UUID, List<BankService.LoanInfo>> activeLoanCache = new HashMap<>();

    private BankGui() {}

    public static void open(Player player, AngelNCore plugin) {
        BankService bank = ServiceRegistry.getBankService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.GOLD_BLOCK, "&eAngel Bank",
                "&7Collateral: company treasury \u2265 50% of loan",
                "&7Max 3 active loans \u2022 5% APR"));

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(10, item(Material.GOLD_NUGGET, "&6Your Balance: &e$" + String.format("%.2f", balance),
                "&7Auto-repay: 10% of balance every hour"));

        if (bank == null) {
            inv.setItem(22, item(Material.BARRIER, "&cBank unavailable"));
        } else {
            List<BankService.LoanInfo> activeLoans = bank.getLoans(player.getUniqueId()).stream()
                    .filter(l -> "active".equals(l.status())).toList();
            activeLoanCache.put(player.getUniqueId(), activeLoans);

            inv.setItem(13, item(Material.PAPER, "&eActive Loans: &f" + activeLoans.size() + " / 3"));

            for (int i = 0; i < Math.min(activeLoans.size(), LOAN_SLOTS.length); i++) {
                BankService.LoanInfo l = activeLoans.get(i);
                inv.setItem(LOAN_SLOTS[i], item(Material.PAPER, "&fLoan #" + l.loanId().substring(0, 8),
                        "&7Amount: &f$" + String.format("%.2f", l.amount()),
                        "&7Remaining: &e$" + String.format("%.2f", l.remaining()),
                        "&7Rate: &f" + String.format("%.1f", l.rate() * 100) + "%",
                        "&7Due: &f" + new java.util.Date(l.dueAt()),
                        "",
                        "&aClick to repay from balance"));
            }

            // Borrow row: predefined amounts
            for (int i = 0; i < BORROW_AMOUNTS.length; i++) {
                inv.setItem(BORROW_SLOTS[i], item(Material.EMERALD, "&aBorrow $" + String.format("%,.0f", BORROW_AMOUNTS[i]),
                        "&7Term: &f" + BORROW_TERMS[i] + " days",
                        "&7Rate: &f5% APR",
                        "&7Collateral: company treasury \u2265 50%",
                        "",
                        "&eClick to borrow"));
            }
        }

        inv.setItem(40, item(Material.OAK_DOOR, "&cBack to Hub"));
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
