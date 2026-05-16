package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class ItemProducedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String companyId;
    private final String itemType;
    private final int quantity;
    private final long factoryId;

    public ItemProducedEvent(String companyId, String itemType, int quantity, long factoryId) {
        this.companyId = companyId;
        this.itemType = itemType;
        this.quantity = quantity;
        this.factoryId = factoryId;
    }

    public String getCompanyId() { return companyId; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public long getFactoryId() { return factoryId; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
