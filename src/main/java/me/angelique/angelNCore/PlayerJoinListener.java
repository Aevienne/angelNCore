package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.AngelNCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final AngelNCore plugin;

    public PlayerJoinListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getEconomyManager().initPlayer(event.getPlayer());
    }
}
