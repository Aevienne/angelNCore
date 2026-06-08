package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.events.*;
import me.angelique.angelNCore.services.StockExchangeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StockEventListener implements Listener {

    private final StockExchangeService exchange;

    public StockEventListener(StockExchangeService exchange) {
        this.exchange = exchange;
    }

    @EventHandler
    public void onItemProduced(ItemProducedEvent event) {
        if (event.getCompanyId() != null && !event.getCompanyId().isBlank()) {
            double revenue = event.getQuantity() * 10.0; // base valuation per item
            exchange.recordRevenue(event.getCompanyId(), revenue);
        }
    }

    @EventHandler
    public void onFactoryDamaged(FactoryDamagedEvent event) {
        if (event.getCompanyId() != null && !event.getCompanyId().isBlank()) {
            exchange.applyDamageModifier(event.getCompanyId(), 0.95);
        }
    }

    @EventHandler
    public void onFactoryRepaired(FactoryRepairedEvent event) {
        if (event.getCompanyId() != null && !event.getCompanyId().isBlank()) {
            exchange.applyDamageModifier(event.getCompanyId(), 1.02);
        }
    }

    @EventHandler
    public void onWarDeclared(WarDeclaredEvent event) {
        exchange.applyWarModifier(event.getAttacker(), 0.90);
        exchange.applyWarModifier(event.getDefender(), 0.90);
    }
}
