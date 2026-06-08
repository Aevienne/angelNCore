package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionCommand implements CommandExecutor, TabCompleter {

    public RegionCommand() {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("angelncore.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        RegionService rs = ServiceRegistry.getRegionService();
        if (rs == null) { sender.sendMessage("Region service unavailable."); return true; }

        if (args.length >= 2 && args[0].equalsIgnoreCase("settype")) {
            RegionService.RegionType type;
            try { type = RegionService.RegionType.valueOf(args[1].toUpperCase()); }
            catch (Exception e) { sender.sendMessage("Invalid type. Use: FERTILE, MINING, FUEL, CHOKEPOINT"); return true; }

            if (sender instanceof Player player) {
                Chunk c = player.getLocation().getChunk();
                rs.setRegionType(c.getWorld().getName(), c.getX(), c.getZ(), type);
                player.sendMessage(ChatColor.GREEN + "Chunk " + c.getX() + "," + c.getZ() + " set to " + type);
            } else {
                sender.sendMessage("Console: provide world x z coordinates.");
            }
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "/region settype <FERTILE|MINING|FUEL|CHOKEPOINT> — set current chunk's region type");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("settype");
        if (args.length == 2 && args[0].equalsIgnoreCase("settype"))
            return List.of("FERTILE", "MINING", "FUEL", "CHOKEPOINT");
        return List.of();
    }
}
