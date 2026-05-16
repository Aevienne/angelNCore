package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class WarEndedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String warId;
    private final String victor;

    public WarEndedEvent(String warId, String victor) {
        this.warId = warId;
        this.victor = victor;
    }

    public String getWarId() { return warId; }
    public String getVictor() { return victor; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
