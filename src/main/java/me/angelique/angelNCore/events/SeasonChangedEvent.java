package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class SeasonChangedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    public enum Season { SPRING, SUMMER, AUTUMN, WINTER }
    private final Season newSeason;

    public SeasonChangedEvent(Season newSeason) {
        this.newSeason = newSeason;
    }

    public Season getNewSeason() { return newSeason; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
