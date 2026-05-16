package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.LogisticsService;
import java.util.*;

public class LogisticsServiceImpl implements LogisticsService {
    private final Map<String, ShipmentData> shipments = new HashMap<>();

    @Override
    public String createShipment(String senderId, String receiverId, String itemType, int quantity) {
        String id = UUID.randomUUID().toString();
        shipments.put(id, new ShipmentData(senderId, receiverId, itemType, quantity));
        return id;
    }

    @Override
    public void markDelivered(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        if (data != null) {
            data.delivered = true;
        }
    }

    @Override
    public void markIntercepted(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        if (data != null) {
            data.intercepted = true;
        }
    }

    @Override
    public boolean isDelivered(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        return data != null && data.delivered;
    }

    @Override
    public int getQuantity(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        return data != null ? data.quantity : 0;
    }

    @Override
    public String getReceiver(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        return data != null ? data.receiverId : null;
    }

    @Override
    public String getSender(String shipmentId) {
        ShipmentData data = shipments.get(shipmentId);
        return data != null ? data.senderId : null;
    }

    private static class ShipmentData {
        String senderId, receiverId, itemType;
        int quantity;
        boolean delivered = false, intercepted = false;

        ShipmentData(String senderId, String receiverId, String itemType, int quantity) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.itemType = itemType;
            this.quantity = quantity;
        }
    }
}
