package me.angelique.angelNCore.economy;

import me.angelique.angelNCore.AngelNCore;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class EconomyManager {

    private final AngelNCore plugin;

    public EconomyManager(AngelNCore plugin) {
        this.plugin = plugin;
    }

    public void initPlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "INSERT OR IGNORE INTO balances (uuid, username, balance) VALUES (?, ?, ?)")) {
            stmt.setString(1, uuid);
            stmt.setString(2, player.getName());
            stmt.setDouble(3, plugin.getConfig().getDouble("economy.starting-balance", 500.0));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to init player: " + e.getMessage());
        }
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "UPDATE balances SET username = ? WHERE uuid = ?")) {
            stmt.setString(1, player.getName());
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update username: " + e.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        if (uuid == null) return 0.0;
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "SELECT balance FROM balances WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get balance: " + e.getMessage());
        }
        return 0.0;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (uuid == null) return false;
        if (!has(uuid, amount)) return false;
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "UPDATE balances SET balance = balance - ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to withdraw: " + e.getMessage());
            return false;
        }
    }

    public void deposit(UUID uuid, double amount) {
        if (uuid == null) return;
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "UPDATE balances SET balance = balance + ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to deposit: " + e.getMessage());
        }
    }

    public void setBalance(UUID uuid, double amount) {
        if (uuid == null) return;
        try (PreparedStatement stmt = plugin.getDatabaseManager().getConnection().prepareStatement(
                "UPDATE balances SET balance = ? WHERE uuid = ?")) {
            stmt.setDouble(1, amount);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to set balance: " + e.getMessage());
        }
    }

    public String formatBalance(double amount) {
        return plugin.getConfig().getString("economy.currency-symbol", "$") + String.format("%.2f", amount);
    }
}
