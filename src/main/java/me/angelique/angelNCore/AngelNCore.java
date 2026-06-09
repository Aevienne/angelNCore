package me.angelique.angelNCore;

import me.angelique.angelNCore.api.StockApiServer;
import me.angelique.angelNCore.commands.BalanceCommand;
import me.angelique.angelNCore.commands.RegionCommand;
import me.angelique.angelNCore.commands.ClaimCommand;
import me.angelique.angelNCore.commands.BankCommand;
import me.angelique.angelNCore.commands.EcoCommand;
import me.angelique.angelNCore.commands.ShopCommand;
import me.angelique.angelNCore.commands.WarCommand;
import me.angelique.angelNCore.commands.MarketCommand;
import me.angelique.angelNCore.commands.StockCommand;
import me.angelique.angelNCore.commands.BackupCommand;
import me.angelique.angelNCore.gui.MenuCommand;
import me.angelique.angelNCore.gui.MenuListener;
import me.angelique.angelNCore.database.DatabaseManager;
import me.angelique.angelNCore.economy.EconomyManager;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.economy.VaultEconomyBridge;
import me.angelique.angelNCore.events.EventBus;
import me.angelique.angelNCore.listeners.PlayerJoinListener;
import me.angelique.angelNCore.listeners.StockEventListener;
import me.angelique.angelNCore.listeners.MilitaryDietListener;
import me.angelique.angelNCore.listeners.CrossListingListener;
import me.angelique.angelNCore.listeners.TerritoryListener;
import me.angelique.angelNCore.services.CompanyService;
import me.angelique.angelNCore.services.*;
import me.angelique.angelNCore.services.impl.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

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

        // Register as Vault economy provider so other plugins see our balances
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultEconomyBridge vaultBridge = new VaultEconomyBridge(economyManager);
            getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class, vaultBridge, this,
                org.bukkit.plugin.ServicePriority.Normal);
            getLogger().info("Registered as Vault economy provider.");
        }

        stockExchange = new StockExchangeServiceImpl(this);
        ServiceRegistry.register(stockExchange);
        StockEventListener stockListener = new StockEventListener(stockExchange);

        bankService = new BankServiceImpl(this);
        ServiceRegistry.register(bankService);

        CrossListingService crossListing = new CrossListingServiceImpl();
        ServiceRegistry.register(crossListing);

        RegionService regionService = new RegionServiceImpl(this);
        ServiceRegistry.register(regionService);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));
        getCommand("war").setExecutor(new WarCommand());
        getCommand("war").setTabCompleter(new WarCommand());
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("bank").setTabCompleter(new BankCommand(this));
        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("claim").setTabCompleter(new ClaimCommand(this));
        getCommand("region").setExecutor(new RegionCommand());
        getCommand("region").setTabCompleter(new RegionCommand());
        getCommand("market").setExecutor(new MarketCommand());
        getCommand("market").setTabCompleter(new MarketCommand());
        getCommand("stock").setExecutor(new StockCommand());
        getCommand("stock").setTabCompleter(new StockCommand());
        getCommand("backup").setExecutor(new BackupCommand(this));
        getCommand("menu").setExecutor(new MenuCommand());

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(stockListener, this);
        getServer().getPluginManager().registerEvents(new MilitaryDietListener(), this);
        getServer().getPluginManager().registerEvents(new CrossListingListener(crossListing), this);
        getServer().getPluginManager().registerEvents(new TerritoryListener(this), this);

        stockApi = new StockApiServer(this, stockExchange);
        ((StockExchangeServiceImpl) stockExchange).setApiServer(stockApi);
        stockApi.start(getConfig().getInt("stock-api-port", 8080));

        marketManager.startDecayTask();

        // Hourly interest tick on ALL active loans + auto-collect from player balance
        getServer().getScheduler().runTaskTimer(this, () -> {
            List<BankService.LoanInfo> active = bankService.getActiveLoans();
            for (BankService.LoanInfo l : active) {
                bankService.processInterest(l.loanId());
                try {
                    UUID borrower = UUID.fromString(l.borrowerUUID());
                    if (economyManager.getBalance(borrower) > 0) {
                        double toRepay = Math.min(economyManager.getBalance(borrower) * 0.1, l.remaining());
                        bankService.repayLoan(l.loanId(), borrower, toRepay);
                    }
                } catch (Exception e) {
                    getLogger().warning("Interest/repay failed for loan " + l.loanId() + ": " + e.getMessage());
                }
            }
        }, 72000L, 72000L);

        // Loan default check every 30 min: mark overdue, liquidate bankrupt companies
        getServer().getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            List<BankService.LoanInfo> active = bankService.getActiveLoans();
            for (BankService.LoanInfo l : active) {
                if (now > l.dueAt()) {
                    UUID borrower;
                    try { borrower = UUID.fromString(l.borrowerUUID()); } catch (Exception e) { continue; }
                    getLogger().warning("Loan " + l.loanId().substring(0, 8) + " defaulted — borrower: " + borrower);
                    try (var ps = databaseManager.getConnection().prepareStatement(
                            "UPDATE loans SET status='defaulted' WHERE loan_id=? AND status='active'")) {
                        ps.setString(1, l.loanId());
                        ps.executeUpdate();
                    } catch (Exception e) { getLogger().warning("Default update failed: " + e.getMessage()); }
                    // Liquidate company if borrower owns one
                    CompanyService cs = ServiceRegistry.getCompanyService();
                    if (cs instanceof CompanyServiceImpl csimpl) {
                        var info = cs.getCompanyByOwner(borrower);
                        if (info != null) {
                            bankService.liquidateCompany(info.id());
                            getLogger().warning("Company " + info.name() + " liquidated due to loan default.");
                        }
                    }
                }
            }
        }, 36000L, 36000L);

        getLogger().info("angelNCore economy enabled!");
    }

    @Override
    public void onDisable() {
        if (stockApi != null) stockApi.stop();
        BackupCommand.backupOnDisable(this);
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
