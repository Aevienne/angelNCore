package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.MarketService;
import java.util.HashMap;
import java.util.Map;

public class MarketServiceImpl implements MarketService {
    private final Map<String, Double> prices = new HashMap<>();
    private final Map<String, Map<Long, Double>> priceHistory = new HashMap<>();
    private final Map<String, Map<String, Double>> regionalModifiers = new HashMap<>();
    private final Map<String, Double> rollingVolume = new HashMap<>();
    private final Map<String, Double> rollingAvgPrice = new HashMap<>();
    private static final double BASE_PRICE = 10.0;
    private static final double ALPHA = 0.05; // smoothing factor for price updates

    @Override
    public double getPrice(String itemType) {
        double base = prices.getOrDefault(itemType, BASE_PRICE);
        double modifier = getRegionalModifier(itemType);
        return base * modifier;
    }

    @Override
    public void updatePrice(String itemType, double newPrice) {
        prices.put(itemType, Math.max(0.01, newPrice));
        recordPriceHistoryPoint(itemType, newPrice);
    }

    @Override
    public void recordTransaction(String itemType, int quantity, double pricePerUnit) {
        if (itemType == null || quantity == 0) return;
        double currentVol = rollingVolume.getOrDefault(itemType, 0.0);
        double currentAvg = rollingAvgPrice.getOrDefault(itemType, pricePerUnit);
        // Weighted moving average
        double newVol = currentVol + Math.abs(quantity);
        double newAvg = ((currentAvg * currentVol) + (pricePerUnit * Math.abs(quantity))) / newVol;
        rollingVolume.put(itemType, newVol);
        rollingAvgPrice.put(itemType, newAvg);
        // Smooth adjustment toward transaction price
        double currentPrice = prices.getOrDefault(itemType, BASE_PRICE);
        double adjusted = currentPrice + ALPHA * (newAvg - currentPrice);
        updatePrice(itemType, adjusted);
    }

    @Override
    public Map<String, Double> getAllPrices() {
        Map<String, Double> result = new HashMap<>();
        for (var entry : prices.entrySet()) {
            result.put(entry.getKey(), getPrice(entry.getKey()));
        }
        return result;
    }

    @Override
    public double getPriceHistory(String itemType, long timestampMs) {
        Map<Long, Double> history = priceHistory.getOrDefault(itemType, new HashMap<>());
        return history.getOrDefault(timestampMs, getPrice(itemType));
    }

    @Override
    public void applyRegionalModifier(String region, String itemType, double modifier) {
        regionalModifiers.computeIfAbsent(itemType, k -> new HashMap<>()).put(region, modifier);
    }

    private double getRegionalModifier(String itemType) {
        Map<String, Double> mods = regionalModifiers.get(itemType);
        if (mods == null || mods.isEmpty()) return 1.0;
        return mods.values().stream().reduce(1.0, (a, b) -> a * b);
    }

    private void recordPriceHistoryPoint(String itemType, double price) {
        priceHistory.computeIfAbsent(itemType, k -> new HashMap<>())
                .put(System.currentTimeMillis(), price);
    }
}
