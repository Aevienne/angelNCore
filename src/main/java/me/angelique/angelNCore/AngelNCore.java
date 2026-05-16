package me.angelique.angelNCore;

import me.angelique.angelNCore.commands.BalanceCommand;
import me.angelique.angelNCore.commands.EcoCommand;
import me.angelique.angelNCore.commands.ShopCommand;
import me.angelique.angelNCore.database.DatabaseManager;
import me.angelique.angelNCore.economy.EconomyManager;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.events.EventBus;
import me.angelique.angelNCore.listeners.PlayerJoinListener;
import me.angelique.angelNCore.services.*;
import me.angelique.angelNCore.services.impl.*;
import org.bukkit.plugin.java.JavaPlugin;

public class AngelNCore extends JavaPlugin {

    private static AngelNCore instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private MarketManager marketManager;
    private EventBus eventBus;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        eventBus = new EventBus();

        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        // Register core services
        ServiceRegistry.register(new MarketServiceImpl());
        ServiceRegistry.register(new CompanyServiceImpl());
        ServiceRegistry.register(new LogisticsServiceImpl());
        ServiceRegistry.register(new MilitaryServiceImpl());
        ServiceRegistry.register(new NutritionServiceImpl());

        economyManager = new EconomyManager(this);
        marketManager = new MarketManager(this);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        marketManager.startDecayTask();

        getLogger().info("angelNCore economy enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.close();
        getLogger().info("angelNCore economy disabled!");
    }

    public static AngelNCore getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
