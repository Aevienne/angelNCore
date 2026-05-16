package me.angelique.angelNCore.domain;

public class Trade {
    private final String id;
    private String seller;
    private String buyer;
    private String itemType;
    private int quantity;
    private double pricePerUnit;
    private long timestamp;
    private boolean completed;

    public Trade(String id, String seller, String buyer, String itemType, int quantity, double pricePerUnit) {
        this.id = id;
        this.seller = seller;
        this.buyer = buyer;
        this.itemType = itemType;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = System.currentTimeMillis();
        this.completed = false;
    }

    public String getId() { return id; }
    public String getSeller() { return seller; }
    public String getBuyer() { return buyer; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    public double getTotalPrice() { return quantity * pricePerUnit; }
    public long getTimestamp() { return timestamp; }
    public boolean isCompleted() { return completed; }
    public void complete() { this.completed = true; }
}
