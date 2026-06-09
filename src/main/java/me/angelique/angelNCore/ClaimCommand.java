package me.angelique.angelNCore.commands;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.gui.ClaimGui;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimCommand implements CommandExecutor, TabCompleter {

    private final AngelNCore plugin;

    public ClaimCommand(AngelNCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        RegionService rs = ServiceRegistry.getRegionService();
        if (rs == null) { player.sendMessage(ChatColor.RED + "Region service unavailable."); return true; }

        if (args.length == 0) { ClaimGui.open(player, plugin); return true; }

        if (args[0].equalsIgnoreCase("info")) {
            Chunk c = player.getLocation().getChunk();
            var owner = rs.getChunkOwner(c.getWorld().getName(), c.getX(), c.getZ());
            var type = rs.getRegionType(c.getWorld().getName(), c.getX(), c.getZ());
            player.sendMessage(ChatColor.GOLD + "Chunk " + c.getX() + "," + c.getZ() +
                " | Type: " + type + " | Owner: " + (owner != null ? owner.toString().substring(0, 8) : "None"));
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            Chunk c = player.getLocation().getChunk();
            String world = c.getWorld().getName();
            int cx = c.getX(), cz = c.getZ();

            if (rs.getChunkOwner(world, cx, cz) != null) {
                player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
                return true;
            }

            int max = plugin.getConfig().getInt("land.max-claims", 10);
            if (rs.getClaimCount(player.getUniqueId()) >= max) {
                player.sendMessage(ChatColor.RED + "Claim limit reached (" + max + ").");
                return true;
            }

            double cost = plugin.getConfig().getDouble("land.claim-cost", 100.0);
            if (!plugin.getEconomyManager().has(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Insufficient funds. Cost: $" + String.format("%.2f", cost));
                return true;
            }
            plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);

            RegionService.RegionType type = args.length > 1 ? parseType(args[1]) : RegionService.RegionType.DEFAULT;
            rs.claimChunk(player.getUniqueId(), world, cx, cz, type);
            player.sendMessage(ChatColor.GREEN + "Chunk claimed! Type: " + type + " | Claims: " + rs.getClaimCount(player.getUniqueId()) + "/" + max);
            return true;
        }

        if (args[0].equalsIgnoreCase("release")) {
            Chunk c = player.getLocation().getChunk();
            String world = c.getWorld().getName();
            int cx = c.getX(), cz = c.getZ();
            boolean ok = rs.releaseChunk(player.getUniqueId(), world, cx, cz);
            player.sendMessage(ok ? ChatColor.GREEN + "Chunk released." : ChatColor.RED + "You don't own this chunk.");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            var claims = rs.getPlayerClaims(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "Your claims (" + claims.size() + "):");
            for (String s : claims) player.sendMessage(ChatColor.YELLOW + "  " + s);
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "/claim info | /claim claim [type] | /claim release | /claim list");
        return true;
    }

    private RegionService.RegionType parseType(String s) {
        try { return RegionService.RegionType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return RegionService.RegionType.DEFAULT; }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("info", "claim", "release", "list");
        if (args.length == 2 && args[0].equalsIgnoreCase("claim"))
            return List.of("FERTILE", "MINING", "FUEL", "CHOKEPOINT");
        return List.of();
    }
}
