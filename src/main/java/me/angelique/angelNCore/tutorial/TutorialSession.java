package me.angelique.angelNCore.tutorial;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.*;

public class TutorialSession {

    public enum Step {
        INTRO, HUB, SHOP, CLAIM, DUEL, BOUNTY, STOCK, BANK, FACTORY, COSMETICS, DONE
    }

    private Step currentStep = Step.INTRO;
    private boolean active = false;
    private Zombie duelBot;

    private double demoBalance = 1000.0;
    private boolean shopDone, duelDone;

    private static final Map<UUID, TutorialSession> sessions = new HashMap<>();

    public TutorialSession() {}

    public static TutorialSession get(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new TutorialSession());
    }

    public static void remove(Player player) {
        TutorialSession s = sessions.remove(player.getUniqueId());
        if (s != null && s.duelBot != null && s.duelBot.isValid()) s.duelBot.remove();
    }

    public Step getStep() { return currentStep; }
    public void setStep(Step step) { this.currentStep = step; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getDemoBalance() { return demoBalance; }
    public void setDemoBalance(double v) { this.demoBalance = v; }
    public boolean isShopDone() { return shopDone; }
    public void setShopDone(boolean v) { this.shopDone = v; }
    public boolean isDuelDone() { return duelDone; }
    public void setDuelDone(boolean v) { this.duelDone = v; }

    public Zombie getDuelBot() { return duelBot; }
    public void setDuelBot(Zombie bot) { this.duelBot = bot; }

    public void reset() {
        this.currentStep = Step.INTRO;
        this.demoBalance = 1000.0;
        this.shopDone = false;
        this.duelDone = false;
        if (duelBot != null && duelBot.isValid()) duelBot.remove();
        this.duelBot = null;
    }
}
