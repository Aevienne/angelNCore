package me.angelique.angelNCore.services;

public interface LogisticsService {
    String createShipment(String senderId, String receiverId, String itemType, int quantity);
    void markDelivered(String shipmentId);
    void markIntercepted(String shipmentId);
    boolean isDelivered(String shipmentId);
    int getQuantity(String shipmentId);
    String getReceiver(String shipmentId);
    String getSender(String shipmentId);
}
