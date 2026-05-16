package me.angelique.angelNCore.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AngelNetworkEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final long timestamp = System.currentTimeMillis();

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
