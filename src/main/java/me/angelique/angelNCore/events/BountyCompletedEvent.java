package me.angelique.angelNCore.events;

import org.bukkit.event.HandlerList;

public class BountyCompletedEvent extends AngelNetworkEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String bountyId;
    private final String hunter;
    private final String target;
    private final double reward;

    public BountyCompletedEvent(String bountyId, String hunter, String target, double reward) {
        this.bountyId = bountyId;
        this.hunter = hunter;
        this.target = target;
        this.reward = reward;
    }

    public String getBountyId() { return bountyId; }
    public String getHunter() { return hunter; }
    public String getTarget() { return target; }
    public double getReward() { return reward; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
