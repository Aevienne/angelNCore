package me.angelique.angelNCore.economy;

import me.angelique.angelNCore.AngelNCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MarketManager {

    private final AngelNCore plugin;

    public MarketManager(AngelNCore plugin) {
        this.plugin = plugin;
        initMarketPrices();
    }

    private void initMarketPrices() {
        Set<String> items = plugin.getConfig().getConfigurationSection("items").getKeys(false);
        for (String item : items) {
            double basePrice = plugin.getConfig().getDouble("items." + item + ".base-price");
            try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                    "INSERT OR IGNORE INTO market_prices (item, current_price, buy_volume, sell_volume, last_updated) VALUES (?, ?, 0, 0, ?)")) {
                stmt.setString(1, item);
                stmt.setDouble(2, basePrice);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to init market price for " + item + ": " + e.getMessage());
            }
        }
    }

    public double getCurrentPrice(String item) {
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "SELECT current_price FROM market_prices WHERE item = ?")) {
            stmt.setString(1, item.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("current_price");
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get price: " + e.getMessage());
        }
        return plugin.getConfig().getDouble("items." + item.toUpperCase() + ".base-price", 10.0);
    }

    public double getSellPrice(String item) {
        return getCurrentPrice(item) * 0.7;
    }

    public void recordBuy(String item, int amount, UUID buyer, String username, double priceEach) {
        item = item.toUpperCase();
        double changeRate = plugin.getConfig().getDouble("shop.price-change-rate", 0.05);
        double maxMultiplier = plugin.getConfig().getDouble("shop.max-price-multiplier", 5.0);
        double basePrice = plugin.getConfig().getDouble("items." + item + ".base-price", 10.0);
        double newPrice = Math.min(getCurrentPrice(item) * (1 + changeRate * amount), basePrice * maxMultiplier);
        updatePrice(item, newPrice, amount, 0);
        logTransaction(buyer, username, "BUY", item, amount, priceEach);
    }

    public void recordSell(String item, int amount, UUID seller, String username, double priceEach) {
        item = item.toUpperCase();
        double changeRate = plugin.getConfig().getDouble("shop.price-change-rate", 0.05);
        double minMultiplier = plugin.getConfig().getDouble("shop.min-price-multiplier", 0.2);
        double basePrice = plugin.getConfig().getDouble("items." + item + ".base-price", 10.0);
        double newPrice = Math.max(getCurrentPrice(item) * (1 - changeRate * amount), basePrice * minMultiplier);
        updatePrice(item, newPrice, 0, amount);
        logTransaction(seller, username, "SELL", item, amount, priceEach);
    }

    private void updatePrice(String item, double newPrice, int buyVol, int sellVol) {
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "UPDATE market_prices SET current_price = ?, buy_volume = buy_volume + ?, sell_volume = sell_volume + ?, last_updated = ? WHERE item = ?")) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, buyVol);
            stmt.setInt(3, sellVol);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, item);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update price: " + e.getMessage());
        }
    }

    private void logTransaction(UUID uuid, String username, String type, String item, int amount, double priceEach) {
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "INSERT INTO transactions (uuid, username, type, item, amount, price_each, total, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, type);
            stmt.setString(4, item);
            stmt.setInt(5, amount);
            stmt.setDouble(6, priceEach);
            stmt.setDouble(7, priceEach * amount);
            stmt.setLong(8, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to log transaction: " + e.getMessage());
        }
    }

    public Map<String, double[]> getAllPrices() {
        Map<String, double[]> prices = new HashMap<>();
        try (Statement stmt = plugin.getDatabaseManager().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item, current_price FROM market_prices")) {
            while (rs.next()) {
                String item = rs.getString("item");
                double price = rs.getDouble("current_price");
                prices.put(item, new double[]{price, getSellPrice(item)});
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get all prices: " + e.getMessage());
        }
        return prices;
    }

    public boolean isValidItem(String item) {
        return plugin.getConfig().contains("items." + item.toUpperCase());
    }

    public String getDisplayName(String item) {
        return plugin.getConfig().getString("items." + item.toUpperCase() + ".display-name", item);
    }

    public void startDecayTask() {
        long interval = plugin.getConfig().getLong("shop.price-decay-interval", 60) * 20L;
        double decayRate = plugin.getConfig().getDouble("shop.price-decay-rate", 0.01);

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<String> items = plugin.getConfig().getConfigurationSection("items").getKeys(false);
                for (String item : items) {
                    double basePrice = plugin.getConfig().getDouble("items." + item + ".base-price");
                    double currentPrice = getCurrentPrice(item);
                    double newPrice = currentPrice + (basePrice - currentPrice) * decayRate;
                    updatePrice(item, newPrice, 0, 0);
                }
            }
        }.runTaskTimerAsynchronously(plugin, interval, interval);
    }
}
