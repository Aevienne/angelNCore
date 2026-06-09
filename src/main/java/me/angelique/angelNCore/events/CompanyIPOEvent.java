package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class CompanyIPOEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String companyId;
    private final String companyName;
    private final int totalShares;
    private final double initialSharePrice;

    public CompanyIPOEvent(String companyId, String companyName, int totalShares, double initialSharePrice) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.totalShares = totalShares;
        this.initialSharePrice = initialSharePrice;
    }

    public String getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
    public int getTotalShares() { return totalShares; }
    public double getInitialSharePrice() { return initialSharePrice; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
