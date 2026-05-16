package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class WarDeclaredEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String warId;
    private final String attacker;
    private final String defender;

    public WarDeclaredEvent(String warId, String attacker, String defender) {
        this.warId = warId;
        this.attacker = attacker;
        this.defender = defender;
    }

    public String getWarId() { return warId; }
    public String getAttacker() { return attacker; }
    public String getDefender() { return defender; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
