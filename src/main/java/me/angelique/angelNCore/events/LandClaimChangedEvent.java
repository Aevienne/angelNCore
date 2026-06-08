package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

import java.util.UUID;

public class LandClaimChangedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String regionId;
    private final UUID ownerUUID;
    private final String action; // "CLAIM" or "RELEASE"

    public LandClaimChangedEvent(String regionId, UUID ownerUUID, String action) {
        this.regionId = regionId;
        this.ownerUUID = ownerUUID;
        this.action = action;
    }

    public String getRegionId() { return regionId; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getAction() { return action; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
