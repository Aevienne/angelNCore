package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerDietChangedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final UUID playerUUID;
    private final int dietScore;
    private final boolean balanced;

    public PlayerDietChangedEvent(UUID playerUUID, int dietScore, boolean balanced) {
        this.playerUUID = playerUUID;
        this.dietScore = dietScore;
        this.balanced = balanced;
    }

    public UUID getPlayerUUID() { return playerUUID; }
    public int getDietScore() { return dietScore; }
    public boolean isBalanced() { return balanced; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
