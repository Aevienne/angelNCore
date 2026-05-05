package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final EconomyManager economy;

    public BalanceCommand(AngelNCore plugin) {
        this.economy = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Specify a player name.");
                return true;
            }
            economy.initPlayer(player);
            player.sendMessage(ChatColor.GOLD + "Your balance: " + ChatColor.GREEN
                    + economy.formatBalance(economy.getBalance(player.getUniqueId())));
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or not online.");
                return true;
            }
            economy.initPlayer(target);
            sender.sendMessage(ChatColor.GOLD + target.getName() + "'s balance: " + ChatColor.GREEN
                    + economy.formatBalance(economy.getBalance(target.getUniqueId())));
        }
        return true;
    }
}
