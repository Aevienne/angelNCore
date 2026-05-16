package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.MarketService;
import java.util.HashMap;
import java.util.Map;

public class MarketServiceImpl implements MarketService {
    private final Map<String, Double> prices = new HashMap<>();
    private final Map<String, Map<Long, Double>> priceHistory = new HashMap<>();
    private final Map<String, Map<String, Double>> regionalModifiers = new HashMap<>();

    @Override
    public double getPrice(String itemType) {
        return prices.getOrDefault(itemType, 10.0);
    }

    @Override
    public void updatePrice(String itemType, double newPrice) {
        prices.put(itemType, Math.max(0, newPrice));
        recordPriceHistoryPoint(itemType, newPrice);
    }

    @Override
    public void recordTransaction(String itemType, int quantity, double pricePerUnit) {
        // Eventually: feed into price adjustment logic
    }

    @Override
    public Map<String, Double> getAllPrices() {
        return new HashMap<>(prices);
    }

    @Override
    public double getPriceHistory(String itemType, long timestampMs) {
        Map<Long, Double> history = priceHistory.getOrDefault(itemType, new HashMap<>());
        return history.getOrDefault(timestampMs, getPrice(itemType));
    }

    @Override
    public void applyRegionalModifier(String region, String itemType, double modifier) {
        if (!regionalModifiers.containsKey(itemType)) {
            regionalModifiers.put(itemType, new HashMap<>());
        }
        regionalModifiers.get(itemType).put(region, modifier);
    }

    private void recordPriceHistoryPoint(String itemType, double price) {
        priceHistory.computeIfAbsent(itemType, k -> new HashMap<>())
                .put(System.currentTimeMillis(), price);
    }
}
