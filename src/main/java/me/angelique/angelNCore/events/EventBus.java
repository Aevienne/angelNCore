package me.angelique.angelNCore.events;

import org.bukkit.Bukkit;

public class EventBus {
    public static void publish(AngelNetworkEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
