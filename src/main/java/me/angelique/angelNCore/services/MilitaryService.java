package me.angelique.angelNCore.services;

public interface MilitaryService {
    String declareWar(String attacker, String defender);
    void endWar(String warId);
    boolean isAtWar(String factionId);
    double getWarUpkeepCost(String warId);
    void registerFactory(long factoryId, String ownerCompanyId);
    void damageFactory(long factoryId, int damage);
    void repairFactory(long factoryId, int repairAmount);
}
