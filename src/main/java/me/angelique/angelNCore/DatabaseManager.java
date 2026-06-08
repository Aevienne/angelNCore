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
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS listed_companies (
                    company_id TEXT PRIMARY KEY,
                    total_shares INTEGER NOT NULL DEFAULT 0,
                    current_price REAL NOT NULL DEFAULT 0,
                    listed_at LONG NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stock_orders (
                    order_id TEXT PRIMARY KEY,
                    company_id TEXT NOT NULL,
                    player_uuid TEXT NOT NULL,
                    type TEXT NOT NULL,
                    shares INTEGER NOT NULL,
                    price REAL NOT NULL,
                    status TEXT NOT NULL DEFAULT 'open',
                    placed_at LONG NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stock_holdings (
                    player_uuid TEXT NOT NULL,
                    company_id TEXT NOT NULL,
                    shares INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (player_uuid, company_id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stock_price_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    company_id TEXT NOT NULL,
                    timestamp LONG NOT NULL,
                    open REAL NOT NULL,
                    high REAL NOT NULL,
                    low REAL NOT NULL,
                    close REAL NOT NULL,
                    volume INTEGER NOT NULL DEFAULT 0
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS company_revenue (
                    company_id TEXT NOT NULL,
                    date TEXT NOT NULL,
                    revenue REAL NOT NULL DEFAULT 0,
                    PRIMARY KEY (company_id, date)
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
