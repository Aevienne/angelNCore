package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.events.*;
import me.angelique.angelNCore.services.CompanyService;
import me.angelique.angelNCore.services.MarketService;
import me.angelique.angelNCore.services.StockExchangeService;
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

    // ── Production & Factory ──────────────────────────────────────────

    @EventHandler
    public void onItemProduced(ItemProducedEvent event) {
        if (event.getCompanyId() != null && !event.getCompanyId().isBlank()) {
            MarketService ms = me.angelique.angelNCore.services.ServiceRegistry.getMarketService();
            double unitPrice = ms != null ? ms.getPrice(event.getItemType()) : 10.0;
            double revenue = event.getQuantity() * unitPrice;
            exchange.recordRevenue(event.getCompanyId(), revenue);
            exchange.distributeDividends(event.getCompanyId(), revenue);
            if (ms != null) ms.recordTransaction(event.getItemType(), event.getQuantity(), unitPrice);
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

    // ── War ───────────────────────────────────────────────────────────

    @EventHandler
    public void onWarDeclared(WarDeclaredEvent event) {
        exchange.applyWarModifier(event.getAttacker(), 0.90);
        exchange.applyWarModifier(event.getDefender(), 0.90);
    }

    @EventHandler
    public void onWarEnded(WarEndedEvent event) {
        if (event.getVictor() != null && !event.getVictor().isBlank()) {
            exchange.applyWarModifier(event.getVictor(), 1.15);
        }
    }

    // ── Trade & Logistics ─────────────────────────────────────────────

    @EventHandler
    public void onTradeCompleted(TradeCompletedEvent event) {
        if (event.getSeller() != null) applyLogisticsBonus(event.getSeller(), 1.01);
    }

    @EventHandler
    public void onShipmentIntercepted(ShipmentInterceptedEvent event) {
        if (event.getSender() != null) applyLogisticsPenalty(event.getSender(), 0.93);
    }

    @EventHandler
    public void onContractBreached(ContractBreachedEvent event) {
        applyLogisticsPenalty(event.getBreachingParty(), 0.85);
        if (event.getWrongedParty() != null) applyLogisticsBonus(event.getWrongedParty(), 1.03);
    }

    // ── Auction & IPO ─────────────────────────────────────────────────

    @EventHandler
    public void onAuctionSale(AuctionSaleEvent event) {
        if (event.getSeller() != null) {
            String cid = findCompanyForString(event.getSeller());
            if (cid != null) exchange.recordRevenue(cid, event.getFinalPrice() * 0.05);
        }
        if (event.getBuyer() != null) {
            String cid = findCompanyForString(event.getBuyer());
            if (cid != null) exchange.recordRevenue(cid, event.getFinalPrice() * 0.01);
        }
    }

    @EventHandler
    public void onCompanyIPO(CompanyIPOEvent event) {
        if (!exchange.isListed(event.getCompanyId())) {
            exchange.listCompany(event.getCompanyId(), event.getCompanyName(), event.getTotalShares(), event.getInitialSharePrice());
        }
    }

    // ── Bounty & Duel ─────────────────────────────────────────────────

    @EventHandler
    public void onBountyCompleted(BountyCompletedEvent event) {
        if (event.getHunter() != null) {
            String cid = findCompanyForString(event.getHunter());
            if (cid != null) exchange.recordRevenue(cid, event.getReward() * 0.03);
        }
    }

    @EventHandler
    public void onDuelCompleted(DuelCompletedEvent event) {
        if (event.getWinner() != null) {
            String cid = findCompanyForString(event.getWinner());
            if (cid != null) exchange.recordRevenue(cid, 50.0);
        }
        if (event.getLoser() != null) {
            String cid = findCompanyForString(event.getLoser());
            if (cid != null) exchange.recordRevenue(cid, 5.0);
        }
    }

    // ── Land & Region ─────────────────────────────────────────────────

    @EventHandler
    public void onLandClaimChanged(LandClaimChangedEvent event) {
        String cid = findCompanyForUUID(event.getOwnerUUID());
        if (cid != null) {
            if ("CLAIM".equals(event.getAction())) {
                exchange.applyWarModifier(cid, 1.005);
            } else if ("RELEASE".equals(event.getAction())) {
                exchange.applyWarModifier(cid, 0.995);
            }
        }
    }

    // ── Diet & Morale ─────────────────────────────────────────────────

    @EventHandler
    public void onPlayerDietChanged(PlayerDietChangedEvent event) {
        String cid = findCompanyForUUID(event.getPlayerUUID());
        if (cid != null) {
            double factor = event.isBalanced() ? 1.008 : 0.995;
            exchange.applyDamageModifier(cid, factor);
        }
    }

    // ── Season ────────────────────────────────────────────────────────

    @EventHandler
    public void onSeasonChanged(SeasonChangedEvent event) {
        double factor = switch (event.getNewSeason()) {
            case SPRING -> 1.03;
            case SUMMER -> 1.01;
            case AUTUMN -> 0.97;
            case WINTER -> 0.92;
        };
        for (var info : exchange.listCompanies()) {
            exchange.applySeasonModifier(info.companyId(), factor);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void applyLogisticsBonus(String ownerStr, double factor) {
        String cid = findCompanyForString(ownerStr);
        if (cid != null && exchange instanceof StockExchangeServiceImpl se) {
            se.applyLogisticsModifier(cid, factor);
        }
    }

    private void applyLogisticsPenalty(String ownerStr, double factor) {
        String cid = findCompanyForString(ownerStr);
        if (cid != null && exchange instanceof StockExchangeServiceImpl se) {
            se.applyLogisticsModifier(cid, factor);
        }
    }

    private String findCompanyForString(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            UUID uid = UUID.fromString(str);
            return findCompanyForUUID(uid);
        } catch (IllegalArgumentException e) {
            // Not a UUID — treat as companyId directly
            if (exchange.isListed(str)) return str;
        }
        return null;
    }

    private String findCompanyForUUID(UUID uid) {
        CompanyService cs = me.angelique.angelNCore.services.ServiceRegistry.getCompanyService();
        if (cs instanceof CompanyServiceImpl csi) {
            return csi.getCompanyForPlayer(uid);
        }
        return null;
    }
}
