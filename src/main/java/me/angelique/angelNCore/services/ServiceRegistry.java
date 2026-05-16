package me.angelique.angelNCore.services;

public class ServiceRegistry {
    private static MarketService marketService;
    private static CompanyService companyService;
    private static LogisticsService logisticsService;
    private static MilitaryService militaryService;
    private static NutritionService nutritionService;

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
}
