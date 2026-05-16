package me.angelique.angelNCore.domain;

import java.util.*;

public class Company {
    private final String id;
    private String name;
    private UUID founder;
    private final Set<UUID> members = new HashSet<>();
    private double balance;
    private final Set<Long> ownedFactories = new HashSet<>();
    private final Map<String, Integer> stockpile = new HashMap<>();

    public Company(String id, String name, UUID founder) {
        this.id = id;
        this.name = name;
        this.founder = founder;
        this.members.add(founder);
        this.balance = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public UUID getFounder() { return founder; }
    public Set<UUID> getMembers() { return new HashSet<>(members); }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public void addBalance(double amount) { this.balance += amount; }
    public Set<Long> getOwnedFactories() { return new HashSet<>(ownedFactories); }
    public void addFactory(long factoryId) { ownedFactories.add(factoryId); }
    public void removeFactory(long factoryId) { ownedFactories.remove(factoryId); }
    public Map<String, Integer> getStockpile() { return new HashMap<>(stockpile); }
    public void addToStockpile(String itemType, int quantity) {
        stockpile.merge(itemType, quantity, Integer::sum);
    }
    public void removeFromStockpile(String itemType, int quantity) {
        stockpile.compute(itemType, (k, v) -> v == null ? 0 : Math.max(0, v - quantity));
    }
}
