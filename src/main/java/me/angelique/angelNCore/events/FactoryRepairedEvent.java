package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class FactoryRepairedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final long factoryId;
    private final String companyId;
    private final int repairAmount;

    public FactoryRepairedEvent(long factoryId, String companyId, int repairAmount) {
        this.factoryId = factoryId;
        this.companyId = companyId;
        this.repairAmount = repairAmount;
    }

    public long getFactoryId() { return factoryId; }
    public String getCompanyId() { return companyId; }
    public int getRepairAmount() { return repairAmount; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
