package me.angelique.angelNCore.services;

import java.util.List;
import java.util.UUID;

public interface StockExchangeService {

    void listCompany(String companyId, String companyName, int totalShares, double initialPrice);
    boolean isListed(String companyId);

    record CompanyInfo(String companyId, String name, int totalShares, double currentPrice, int volume) {}
    record OrderInfo(String orderId, String companyId, UUID playerUUID, String type, int shares, double price, String status) {}
    record PriceCandle(long timestamp, double open, double high, double low, double close, int volume) {}

    CompanyInfo getCompanyInfo(String companyId);
    List<CompanyInfo> listCompanies();
    List<PriceCandle> getPriceHistory(String companyId);

    String placeOrder(UUID playerUUID, String companyId, String type, int shares, double price);
    boolean cancelOrder(String orderId, UUID playerUUID);
    List<OrderInfo> getOrderBook(String companyId);
    int getHolding(UUID playerUUID, String companyId);

    void recordRevenue(String companyId, double revenue);
    void distributeDividends(String companyId, double amount);
    void applyDamageModifier(String companyId, double factor);
    void applyWarModifier(String companyId, double factor);
    void applySeasonModifier(String companyId, double factor);

    // Auth
    String generateToken(UUID playerUUID);
    UUID validateToken(String token);
}
