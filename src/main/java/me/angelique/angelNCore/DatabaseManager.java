package me.angelique.angelNCore.database;

import me.angelique.angelNCore.AngelNCore;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final AngelNCore plugin;
    private Connection connection;

    public DatabaseManager(AngelNCore plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "economy.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            createTables();
            plugin.getLogger().info("Database connected.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS balances (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS market_prices (
                    item TEXT PRIMARY KEY,
                    current_price REAL NOT NULL,
                    buy_volume INTEGER NOT NULL DEFAULT 0,
                    sell_volume INTEGER NOT NULL DEFAULT 0,
                    last_updated LONG NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    username TEXT NOT NULL,
                    type TEXT NOT NULL,
                    item TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    price_each REAL NOT NULL,
                    total REAL NOT NULL,
                    timestamp LONG NOT NULL
                )
            """);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing database.", e);
        }
    }
}
