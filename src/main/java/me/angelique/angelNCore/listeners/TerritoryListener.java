package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class TerritoryListener implements Listener {

    private final AngelNCore plugin;
    private final Map<UUID, String> playerChunkKey = new HashMap<>();

    public TerritoryListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        Player player = event.getPlayer();
        UUID pid = player.getUniqueId();
        String world = to.getWorld().getName();
        int cx = to.getX(), cz = to.getZ();
        String key = world + ":" + cx + "," + cz;

        if (key.equals(playerChunkKey.get(pid))) return;
        playerChunkKey.put(pid, key);

        RegionService rs = ServiceRegistry.getRegionService();
        UUID owner = rs != null ? rs.getChunkOwner(world, cx, cz) : null;

        if (owner == null) {
            player.showTitle(Title.title(
                Component.text("§7Wilderness"),
                Component.text("§8Chunk " + cx + ", " + cz),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1200), java.time.Duration.ofMillis(400))
            ));
        } else if (owner.equals(pid)) {
            player.showTitle(Title.title(
                Component.text("§aYour Territory"),
                Component.text("§7Chunk " + cx + ", " + cz),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1200), java.time.Duration.ofMillis(400))
            ));
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            String name = op.getName() != null ? op.getName() : owner.toString().substring(0, 8);
            player.showTitle(Title.title(
                Component.text("§c" + name + "'s Territory"),
                Component.text("§7Chunk " + cx + ", " + cz),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1800), java.time.Duration.ofMillis(400))
            ));
            // Show corner particles for foreign territory
            showChunkCorners(player, to);
        }
    }

    private void showChunkCorners(Player player, Chunk chunk) {
        World world = chunk.getWorld();
        int cx = chunk.getX() << 4;
        int cz = chunk.getZ() << 4;
        double y = player.getLocation().getY();

        for (int dx = 0; dx <= 16; dx += 16) {
            for (int dz = 0; dz <= 16; dz += 16) {
                for (double dy = -1; dy <= 2; dy += 1.5) {
                    Location loc = new Location(world, cx + dx, y + dy, cz + dz);
                    player.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
