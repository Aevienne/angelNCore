package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class DuelCompletedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String duelId;
    private final String winner;
    private final String loser;

    public DuelCompletedEvent(String duelId, String winner, String loser) {
        this.duelId = duelId;
        this.winner = winner;
        this.loser = loser;
    }

    public String getDuelId() { return duelId; }
    public String getWinner() { return winner; }
    public String getLoser() { return loser; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
