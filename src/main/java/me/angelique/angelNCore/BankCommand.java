package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.gui.BankGui;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.CompanyService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BankCommand implements CommandExecutor, TabCompleter {

    private final AngelNCore plugin;

    public BankCommand(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        BankService bank = ServiceRegistry.getBankService();
        if (bank == null) {
            player.sendMessage(ChatColor.RED + "Banking system unavailable.");
            return true;
        }

        if (args.length == 0) { BankGui.open(player, plugin); return true; }
        if (args[0].equalsIgnoreCase("loans")) {
            var loans = bank.getLoans(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "=== Your Loans ===");
            for (var l : loans) {
                player.sendMessage(ChatColor.YELLOW + l.loanId().substring(0, 8) + ": $" +
                    String.format("%.2f", l.remaining()) + " / $" + String.format("%.2f", l.amount()) +
                    " @ " + (l.rate() * 100) + "% - " + l.status());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("borrow")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /bank borrow <amount> <termDays>");
                return true;
            }
            double amount = Double.parseDouble(args[1]);
            int term = Integer.parseInt(args[2]);
            double rate = plugin.getConfig().getDouble("bank.default-rate", 0.05);
            String id = bank.createLoan(player.getUniqueId(), amount, rate, term);
            if (id == null || id.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Loan rejected. Check logs for reason.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Loan approved: $" + String.format("%.2f", amount) +
                    " for " + term + " days at " + (rate * 100) + "% APR. ID: " + id.substring(0, 8));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("repay")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /bank repay <loanId> <amount>");
                return true;
            }
            String loanId = args[1];
            double amount = Double.parseDouble(args[2]);
            boolean ok = bank.repayLoan(loanId, player.getUniqueId(), amount);
            player.sendMessage(ok ? ChatColor.GREEN + "Repaid $" + String.format("%.2f", amount)
                : ChatColor.RED + "Repayment failed.");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "/bank loans | /bank borrow <amt> <days> | /bank repay <loanId> <amt>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("loans", "borrow", "repay");
        return List.of();
    }
}
