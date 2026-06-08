package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.events.EventBus;
import me.angelique.angelNCore.events.WarDeclaredEvent;
import me.angelique.angelNCore.events.WarEndedEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.UUID;

public class WarCommand implements CommandExecutor, TabCompleter {

    public WarCommand() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /war <declare|end> [args]");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("declare")) {
            return declare(sender, args);
        } else if (sub.equals("end")) {
            return end(sender, args);
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use declare or end.");
            return true;
        }
    }

    private boolean declare(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /war declare <attacker> <defender>");
            return true;
        }
        String attacker = args[1];
        String defender = args[2];
        String warId = "war-" + UUID.randomUUID().toString().substring(0, 8);
        EventBus.publish(new WarDeclaredEvent(warId, attacker, defender));
        sender.sendMessage(ChatColor.GREEN + "War declared: " + ChatColor.RED + attacker + ChatColor.YELLOW + " vs " + ChatColor.RED + defender);
        return true;
    }

    private boolean end(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /war end <warId> <victor>");
            return true;
        }
        String warId = args[1];
        String victor = args[2];
        EventBus.publish(new WarEndedEvent(warId, victor));
        sender.sendMessage(ChatColor.GREEN + "War ended: " + ChatColor.GOLD + warId + ChatColor.GREEN + " — victor: " + ChatColor.GOLD + victor);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("declare", "end");
        return List.of();
    }
}
