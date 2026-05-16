package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class FactoryDamagedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final long factoryId;
    private final String companyId;
    private final int damageAmount;

    public FactoryDamagedEvent(long factoryId, String companyId, int damageAmount) {
        this.factoryId = factoryId;
        this.companyId = companyId;
        this.damageAmount = damageAmount;
    }

    public long getFactoryId() { return factoryId; }
    public String getCompanyId() { return companyId; }
    public int getDamageAmount() { return damageAmount; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
