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
    private final Map<UUID, UUID> playerLastOwner = new HashMap<>();

    public TerritoryListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk to = event.getTo().getChunk();
        Chunk from = event.getFrom().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        Player player = event.getPlayer();
        UUID pid = player.getUniqueId();
        String world = to.getWorld().getName();

        RegionService rs = ServiceRegistry.getRegionService();
        UUID owner = rs != null ? rs.getChunkOwner(world, to.getX(), to.getZ()) : null;
        UUID lastOwner = playerLastOwner.get(pid);

        // Only notify when owner changes (including to/from null)
        if (Objects.equals(owner, lastOwner)) return;
        playerLastOwner.put(pid, owner);

        if (owner == null) {
            player.showTitle(Title.title(
                Component.text("§7Wilderness"),
                Component.text("§8Unclaimed territory"),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1200), java.time.Duration.ofMillis(400))
            ));
        } else if (owner.equals(pid)) {
            player.showTitle(Title.title(
                Component.text("§aYour Territory"),
                Component.text("§8Welcome home"),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1200), java.time.Duration.ofMillis(400))
            ));
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            String name = op.getName() != null ? op.getName() : owner.toString().substring(0, 8);
            player.showTitle(Title.title(
                Component.text("§c" + name + "'s Territory"),
                Component.text("§8Entering claimed land"),
                Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1800), java.time.Duration.ofMillis(400))
            ));
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
