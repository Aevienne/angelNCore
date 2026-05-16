package me.angelique.angelNCore.services;

import java.util.Map;

public interface MarketService {
    double getPrice(String itemType);
    void updatePrice(String itemType, double newPrice);
    void recordTransaction(String itemType, int quantity, double pricePerUnit);
    Map<String, Double> getAllPrices();
    double getPriceHistory(String itemType, long timestampMs);
    void applyRegionalModifier(String region, String itemType, double modifier);
}
