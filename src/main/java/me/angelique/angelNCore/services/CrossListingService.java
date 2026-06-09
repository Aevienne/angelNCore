package me.angelique.angelNCore.services;

import java.util.UUID;

public interface CrossListingService {
    record UnifiedListing(String listingId, String itemType, int quantity, double price, String source, String owner, long timestamp) {}

    void registerListing(String listingId, String itemType, int quantity, double price, String source, String owner);
    void removeListing(String listingId);
    void recordCrossVenueTrade(String itemType, int quantity, double price, String source, String target);
    double getBestPrice(String itemType, int quantity);
    String getLiquidityReport(String itemType);
}
