package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoCommand implements CommandExecutor {

    private final EconomyManager economy;

    public EcoCommand(AngelNCore plugin) {
        this.economy = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("economy.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco [give|take|set] [player] [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a positive number.");
            return true;
        }

        economy.initPlayer(target);

        switch (args[0].toLowerCase()) {
            case "give" -> {
                economy.deposit(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "Gave " + economy.formatBalance(amount) + " to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You received " + economy.formatBalance(amount) + " from an admin.");
            }
            case "take" -> {
                economy.withdraw(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "Took " + economy.formatBalance(amount) + " from " + target.getName());
                target.sendMessage(ChatColor.RED + "An admin removed " + economy.formatBalance(amount) + " from your balance.");
            }
            case "set" -> {
                economy.setBalance(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to " + economy.formatBalance(amount));
                target.sendMessage(ChatColor.YELLOW + "Your balance was set to " + economy.formatBalance(amount) + " by an admin.");
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /eco [give|take|set] [player] [amount]");
        }
        return true;
    }
}
