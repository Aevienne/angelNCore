package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.CrossListingService;
import me.angelique.angelNCore.services.MarketService;
import me.angelique.angelNCore.services.ServiceRegistry;

import java.util.*;

public class CrossListingServiceImpl implements CrossListingService {

    private final Map<String, UnifiedListing> listings = new LinkedHashMap<>();
    private final Map<String, List<Double>> venuePrices = new HashMap<>(); // itemType -> [prices]

    @Override
    public void registerListing(String listingId, String itemType, int quantity, double price, String source, String owner) {
        listings.put(listingId, new UnifiedListing(listingId, itemType, quantity, price, source, owner, System.currentTimeMillis()));
        venuePrices.computeIfAbsent(itemType, k -> new ArrayList<>()).add(price);
    }

    @Override
    public void removeListing(String listingId) {
        UnifiedListing removed = listings.remove(listingId);
        if (removed != null) {
            List<Double> prices = venuePrices.get(removed.itemType());
            if (prices != null) prices.remove(removed.price());
        }
    }

    @Override
    public void recordCrossVenueTrade(String itemType, int quantity, double price, String source, String target) {
        MarketService ms = ServiceRegistry.getMarketService();
        if (ms != null) ms.recordTransaction(itemType, quantity, price);
    }

    @Override
    public double getBestPrice(String itemType, int quantity) {
        List<Double> prices = venuePrices.getOrDefault(itemType, Collections.emptyList());
        return prices.stream().min(Double::compare).orElse(10.0);
    }

    @Override
    public String getLiquidityReport(String itemType) {
        List<Double> prices = venuePrices.getOrDefault(itemType, Collections.emptyList());
        if (prices.isEmpty()) return "No cross-venue activity for " + itemType;
        double avg = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double best = getBestPrice(itemType, 1);
        return String.format("%s: best $%.2f, avg $%.2f, %d venues", itemType, best, avg, prices.size());
    }

    public Collection<UnifiedListing> getAllListings() {
        return new ArrayList<>(listings.values());
    }

    public Map<String, Double> getCrossVenuePrices() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (var entry : venuePrices.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.put(entry.getKey(), getBestPrice(entry.getKey(), 1));
            }
        }
        return result;
    }
}
