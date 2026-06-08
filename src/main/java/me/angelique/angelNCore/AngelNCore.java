package me.angelique.angelNCore;

import me.angelique.angelNCore.api.StockApiServer;
import me.angelique.angelNCore.commands.BalanceCommand;
import me.angelique.angelNCore.commands.BankCommand;
import me.angelique.angelNCore.commands.EcoCommand;
import me.angelique.angelNCore.commands.ShopCommand;
import me.angelique.angelNCore.commands.WarCommand;
import me.angelique.angelNCore.database.DatabaseManager;
import me.angelique.angelNCore.economy.EconomyManager;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.events.EventBus;
import me.angelique.angelNCore.listeners.PlayerJoinListener;
import me.angelique.angelNCore.listeners.StockEventListener;
import me.angelique.angelNCore.services.*;
import me.angelique.angelNCore.services.impl.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AngelNCore extends JavaPlugin {

    private static AngelNCore instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private MarketManager marketManager;
    private EventBus eventBus;
    private StockExchangeService stockExchange;
    private BankService bankService;
    private StockApiServer stockApi;

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

        stockExchange = new StockExchangeServiceImpl(this);
        ServiceRegistry.register(stockExchange);
        StockEventListener stockListener = new StockEventListener(stockExchange);

        bankService = new BankServiceImpl(this);
        ServiceRegistry.register(bankService);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));
        getCommand("war").setExecutor(new WarCommand());
        getCommand("war").setTabCompleter(new WarCommand());
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("bank").setTabCompleter(new BankCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(stockListener, this);

        stockApi = new StockApiServer(this, stockExchange);
        ((StockExchangeServiceImpl) stockExchange).setApiServer(stockApi);
        stockApi.start(getConfig().getInt("stock-api-port", 8080));

        marketManager.startDecayTask();

        // Daily interest & bankruptcy tick (every hour)
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<BankService.LoanInfo> overdue = bankService.getDefaultedLoans();
            for (BankService.LoanInfo l : overdue) {
                bankService.processInterest(l.loanId());
            }
        }, 72000L, 72000L);

        getLogger().info("angelNCore economy enabled!");
    }

    @Override
    public void onDisable() {
        if (stockApi != null) stockApi.stop();
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
