package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.MilitaryService;
import java.util.*;

public class MilitaryServiceImpl implements MilitaryService {
    private final Map<String, WarData> wars = new HashMap<>();
    private final Map<Long, FactoryData> factories = new HashMap<>();

    @Override
    public String declareWar(String attacker, String defender) {
        String id = UUID.randomUUID().toString();
        wars.put(id, new WarData(attacker, defender));
        return id;
    }

    @Override
    public void endWar(String warId) {
        wars.remove(warId);
    }

    @Override
    public boolean isAtWar(String factionId) {
        return wars.values().stream()
                .anyMatch(w -> w.attacker.equals(factionId) || w.defender.equals(factionId));
    }

    @Override
    public double getWarUpkeepCost(String warId) {
        WarData war = wars.get(warId);
        return war != null ? 100.0 : 0; // Placeholder
    }

    @Override
    public void registerFactory(long factoryId, String ownerCompanyId) {
        factories.put(factoryId, new FactoryData(ownerCompanyId));
    }

    @Override
    public void damageFactory(long factoryId, int damage) {
        FactoryData factory = factories.get(factoryId);
        if (factory != null) {
            factory.damage += damage;
        }
    }

    @Override
    public void repairFactory(long factoryId, int repairAmount) {
        FactoryData factory = factories.get(factoryId);
        if (factory != null) {
            factory.damage = Math.max(0, factory.damage - repairAmount);
        }
    }

    private static class WarData {
        String attacker, defender;
        long startTime = System.currentTimeMillis();

        WarData(String attacker, String defender) {
            this.attacker = attacker;
            this.defender = defender;
        }
    }

    private static class FactoryData {
        String ownerCompanyId;
        int damage = 0;

        FactoryData(String ownerCompanyId) {
            this.ownerCompanyId = ownerCompanyId;
        }
    }
}
