package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class ShipmentInterceptedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String shipmentId;
    private final String sender;
    private final String receiver;
    private final String itemType;
    private final int quantity;

    public ShipmentInterceptedEvent(String shipmentId, String sender, String receiver, String itemType, int quantity) {
        this.shipmentId = shipmentId;
        this.sender = sender;
        this.receiver = receiver;
        this.itemType = itemType;
        this.quantity = quantity;
    }

    public String getShipmentId() { return shipmentId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
