package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class AuctionSaleEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String auctionId;
    private final String seller;
    private final String buyer;
    private final String itemType;
    private final int quantity;
    private final double finalPrice;

    public AuctionSaleEvent(String auctionId, String seller, String buyer, String itemType, int quantity, double finalPrice) {
        this.auctionId = auctionId;
        this.seller = seller;
        this.buyer = buyer;
        this.itemType = itemType;
        this.quantity = quantity;
        this.finalPrice = finalPrice;
    }

    public String getAuctionId() { return auctionId; }
    public String getSeller() { return seller; }
    public String getBuyer() { return buyer; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public double getFinalPrice() { return finalPrice; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
