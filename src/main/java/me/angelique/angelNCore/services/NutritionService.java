package me.angelique.angelNCore.services;

import java.util.UUID;

public interface NutritionService {
    enum FoodCategory { PROTEIN, GRAINS, FRUITS, VEGETABLES }

    void recordMeal(UUID playerId, FoodCategory category);
    double getDietBonus(UUID playerId);
    boolean isBoosted(UUID playerId, String buffType);
    void resetDiet(UUID playerId);
}
