package me.angelique.angelNCore.services;

public class ServiceRegistry {
    private static MarketService marketService;
    private static CompanyService companyService;
    private static LogisticsService logisticsService;
    private static MilitaryService militaryService;
    private static NutritionService nutritionService;
    private static StockExchangeService stockExchangeService;
    private static BankService bankService;

    public static void register(MarketService service) {
        marketService = service;
    }

    public static void register(CompanyService service) {
        companyService = service;
    }

    public static void register(LogisticsService service) {
        logisticsService = service;
    }

    public static void register(MilitaryService service) {
        militaryService = service;
    }

    public static void register(NutritionService service) {
        nutritionService = service;
    }

    public static void register(StockExchangeService service) {
        stockExchangeService = service;
    }

    public static void register(BankService service) {
        bankService = service;
    }

    public static MarketService getMarketService() {
        return marketService;
    }

    public static CompanyService getCompanyService() {
        return companyService;
    }

    public static LogisticsService getLogisticsService() {
        return logisticsService;
    }

    public static MilitaryService getMilitaryService() {
        return militaryService;
    }

    public static NutritionService getNutritionService() {
        return nutritionService;
    }

    public static StockExchangeService getStockExchangeService() {
        return stockExchangeService;
    }

    public static BankService getBankService() {
        return bankService;
    }
}
