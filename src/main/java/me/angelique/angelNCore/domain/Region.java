package me.angelique.angelNCore.domain;

public class Region {
    private final String id;
    private String name;
    private RegionType type;
    private double fertilityMultiplier;
    private double miningYield;
    private boolean hasOil;

    public enum RegionType { FERTILE, MOUNTAINOUS, FOREST, DESERT, SWAMP }

    public Region(String id, String name, RegionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.fertilityMultiplier = calculateFertility(type);
        this.miningYield = calculateMining(type);
        this.hasOil = type == RegionType.SWAMP || type == RegionType.DESERT;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public RegionType getType() { return type; }
    public double getFertilityMultiplier() { return fertilityMultiplier; }
    public double getMiningYield() { return miningYield; }
    public boolean hasOil() { return hasOil; }

    private double calculateFertility(RegionType type) {
        return switch(type) {
            case FERTILE -> 1.5;
            case FOREST -> 1.2;
            case MOUNTAINOUS -> 0.5;
            case SWAMP -> 0.8;
            case DESERT -> 0.3;
        };
    }

    private double calculateMining(RegionType type) {
        return switch(type) {
            case MOUNTAINOUS -> 1.5;
            case FERTILE -> 0.3;
            case SWAMP -> 0.7;
            case FOREST -> 0.4;
            case DESERT -> 1.0;
        };
    }
}
