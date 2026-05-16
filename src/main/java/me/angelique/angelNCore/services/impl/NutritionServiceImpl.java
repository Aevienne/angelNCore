package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.NutritionService;
import java.util.*;

public class NutritionServiceImpl implements NutritionService {
    private final Map<UUID, DietData> diets = new HashMap<>();

    @Override
    public void recordMeal(UUID playerId, FoodCategory category) {
        DietData diet = diets.computeIfAbsent(playerId, k -> new DietData());
        diet.lastMealTime = System.currentTimeMillis();
        diet.categoryCount.merge(category, 1, Integer::sum);
    }

    @Override
    public double getDietBonus(UUID playerId) {
        DietData diet = diets.get(playerId);
        if (diet == null) return 1.0;
        // Placeholder: base 1.0x with bonus for balanced diet
        if (diet.categoryCount.size() >= 3) return 1.2;
        if (diet.categoryCount.size() >= 2) return 1.1;
        return 1.0;
    }

    @Override
    public boolean isBoosted(UUID playerId, String buffType) {
        DietData diet = diets.get(playerId);
        if (diet == null) return false;
        // Placeholder: buffs expire after 10 minutes of no eating
        long timeSinceLastMeal = System.currentTimeMillis() - diet.lastMealTime;
        return timeSinceLastMeal < 600000;
    }

    @Override
    public void resetDiet(UUID playerId) {
        diets.remove(playerId);
    }

    private static class DietData {
        Map<FoodCategory, Integer> categoryCount = new HashMap<>();
        long lastMealTime = System.currentTimeMillis();
    }
}
