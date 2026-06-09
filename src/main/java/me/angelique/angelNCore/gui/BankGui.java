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

    private BankGui() {}

    public static void open(Player player, AngelNCore plugin) {
        BankService bank = ServiceRegistry.getBankService();
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.GOLD_BLOCK, "&eAngel Bank",
                "&7Take loans against your company",
                "&7Max 3 active loans \u2022 Interest: 5% APR"));

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(10, item(Material.GOLD_NUGGET, "&6Your Balance: &e$" + String.format("%.2f", balance)));

        // Active loans
        List<BankService.LoanInfo> loans = bank.getLoans(player.getUniqueId());
        inv.setItem(13, item(Material.PAPER, "&eActive Loans: &f" + loans.stream().filter(l -> "active".equals(l.status())).count()));

        if (bank == null) {
            inv.setItem(22, item(Material.BARRIER, "&cBank unavailable"));
        } else {
            // Show active loans in slots 19-25
            int slot = 19;
            for (BankService.LoanInfo l : loans) {
                if (slot > 25) break;
                if (!"active".equals(l.status())) continue;
                inv.setItem(slot++, item(Material.PAPER, "&fLoan #" + l.loanId().substring(0, 8),
                        "&7Amount: &f$" + String.format("%.2f", l.amount()),
                        "&7Remaining: &e$" + String.format("%.2f", l.remaining()),
                        "&7Rate: &f" + String.format("%.1f", l.rate() * 100) + "%",
                        "&7Due: &f" + new java.util.Date(l.dueAt()),
                        "",
                        "&eClick to auto-repay"));
            }

            // Borrow
            inv.setItem(29, item(Material.EMERALD, "&aBorrow Money",
                    "&7Usage: /bank borrow <amount> <days>",
                    "&7Collateral: company treasury 50%+ of loan",
                    "",
                    "&eClick for help"));
            // Repay
            inv.setItem(31, item(Material.GOLD_INGOT, "&eRepay Loan",
                    "&7Usage: /bank repay <loanId> <amount>",
                    "&7Auto-repay 10% of balance every hour",
                    "",
                    "&eClick for help"));
            // History
            inv.setItem(33, item(Material.BOOK, "&7Loan History",
                    "&7Defaulted loans block new borrowing",
                    "&7and trigger company liquidation"));
        }

        inv.setItem(40, item(Material.OAK_DOOR, "&cBack to Hub", "&7Return to main menu"));
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
