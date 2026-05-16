package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class ContractBreachedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String contractId;
    private final String breachingParty;
    private final String wrongedParty;

    public ContractBreachedEvent(String contractId, String breachingParty, String wrongedParty) {
        this.contractId = contractId;
        this.breachingParty = breachingParty;
        this.wrongedParty = wrongedParty;
    }

    public String getContractId() { return contractId; }
    public String getBreachingParty() { return breachingParty; }
    public String getWrongedParty() { return wrongedParty; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
