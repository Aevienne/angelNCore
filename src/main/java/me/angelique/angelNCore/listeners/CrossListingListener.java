package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.events.AuctionSaleEvent;
import me.angelique.angelNCore.events.TradeCompletedEvent;
import me.angelique.angelNCore.services.CrossListingService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class CrossListingListener implements Listener {

    private final CrossListingService crossListing;

    public CrossListingListener(CrossListingService crossListing) {
        this.crossListing = crossListing;
    }

    @EventHandler
    public void onAuctionSale(AuctionSaleEvent event) {
        if (event.getItemType() == null) return;
        crossListing.recordCrossVenueTrade(event.getItemType(), event.getQuantity(), event.getFinalPrice(), "auction", "market");
    }

    @EventHandler
    public void onTradeCompleted(TradeCompletedEvent event) {
        if (event.getItemType() == null) return;
        crossListing.recordCrossVenueTrade(event.getItemType(), event.getQuantity(), event.getPricePerUnit(), "tradeshop", "market");
    }
}
