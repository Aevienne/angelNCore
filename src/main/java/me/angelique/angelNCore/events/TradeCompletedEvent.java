package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class TradeCompletedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String tradeId;
    private final String seller;
    private final String buyer;
    private final String itemType;
    private final int quantity;
    private final double pricePerUnit;

    public TradeCompletedEvent(String tradeId, String seller, String buyer, String itemType, int quantity, double pricePerUnit) {
        this.tradeId = tradeId;
        this.seller = seller;
        this.buyer = buyer;
        this.itemType = itemType;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String getTradeId() { return tradeId; }
    public String getSeller() { return seller; }
    public String getBuyer() { return buyer; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
