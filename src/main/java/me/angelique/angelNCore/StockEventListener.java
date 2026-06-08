package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.events.*;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.CompanyService;
import me.angelique.angelNCore.services.impl.StockExchangeServiceImpl;
import me.angelique.angelNCore.services.impl.CompanyServiceImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class StockEventListener implements Listener {

    private final StockExchangeService exchange;

    public StockEventListener(StockExchangeService exchange) {
        this.exchange = exchange;
    }

    @EventHandler
    public void onItemProduced(ItemProducedEvent event) {
        if (event.getCompanyId() != null && !event.getCompanyId().isBlank()) {
            double revenue = event.getQuantity() * 10.0;
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

    @EventHandler
    public void onTradeCompleted(TradeCompletedEvent event) {
        if (event.getSeller() != null) applyLogisticsBonus(event.getSeller(), 1.01);
    }

    private void applyLogisticsBonus(String ownerStr, double factor) {
        if (exchange instanceof StockExchangeServiceImpl se) {
            try {
                UUID uid = UUID.fromString(ownerStr);
                CompanyService cs = me.angelique.angelNCore.services.ServiceRegistry.getCompanyService();
                if (cs instanceof CompanyServiceImpl csi) {
                    String companyId = csi.getCompanyForPlayer(uid);
                    if (companyId != null) se.applyLogisticsModifier(companyId, factor);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
