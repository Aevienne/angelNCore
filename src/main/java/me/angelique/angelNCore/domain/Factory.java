package me.angelique.angelNCore.domain;

public class Factory {
    private final long id;
    private String ownerCompanyId;
    private String itemProduced;
    private int productionRate; // items per minute
    private int healthPoints;
    private final int maxHealth;
    private double fuelConsumption; // per item produced

    public Factory(long id, String ownerCompanyId, String itemProduced, int productionRate) {
        this.id = id;
        this.ownerCompanyId = ownerCompanyId;
        this.itemProduced = itemProduced;
        this.productionRate = productionRate;
        this.maxHealth = 100;
        this.healthPoints = 100;
        this.fuelConsumption = 1.0;
    }

    public long getId() { return id; }
    public String getOwnerCompanyId() { return ownerCompanyId; }
    public String getItemProduced() { return itemProduced; }
    public int getProductionRate() { return productionRate; }
    public int getHealthPoints() { return healthPoints; }
    public int getMaxHealth() { return maxHealth; }
    public double getThroughputModifier() {
        return (double) healthPoints / maxHealth;
    }
    public void damage(int amount) {
        healthPoints = Math.max(0, healthPoints - amount);
    }
    public void repair(int amount) {
        healthPoints = Math.min(maxHealth, healthPoints + amount);
    }
    public double getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(double consumption) { this.fuelConsumption = consumption; }
}
