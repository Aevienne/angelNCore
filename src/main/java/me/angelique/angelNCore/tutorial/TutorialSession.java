package me.angelique.angelNCore.tutorial;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.*;

public class TutorialSession {

    public enum Step {
        INTRO, HUB, SHOP, CLAIM, DUEL, BOUNTY, STOCK, BANK, COSMETICS, DONE
    }

    private final Player player;
    private Step currentStep = Step.INTRO;
    private boolean active = false;
    private Zombie duelBot;
    private final List<String> completedActions = new ArrayList<>();

    // Simulated data
    private double demoBalance = 1000.0;
    private double demoLoan = 0.0;
    private boolean shopCompleted = false;
    private boolean claimCompleted = false;
    private boolean duelCompleted = false;
    private boolean bountyCompleted = false;
    private boolean stockCompleted = false;
    private boolean bankCompleted = false;
    private boolean subGuiOpen = false;

    private static final Map<UUID, TutorialSession> sessions = new HashMap<>();

    public TutorialSession(Player player) {
        this.player = player;
    }

    public static TutorialSession get(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new TutorialSession(player));
    }

    public static void remove(Player player) {
        TutorialSession s = sessions.remove(player.getUniqueId());
        if (s != null && s.duelBot != null && s.duelBot.isValid()) {
            s.duelBot.remove();
        }
    }

    public Player getPlayer() { return player; }
    public Step getStep() { return currentStep; }
    public void setStep(Step step) { this.currentStep = step; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getDemoBalance() { return demoBalance; }
    public void setDemoBalance(double v) { this.demoBalance = v; }
    public double getDemoLoan() { return demoLoan; }
    public void setDemoLoan(double v) { this.demoLoan = v; }

    public Zombie getDuelBot() { return duelBot; }
    public void setDuelBot(Zombie bot) { this.duelBot = bot; }

    public boolean isShopCompleted() { return shopCompleted; }
    public void setShopCompleted(boolean v) { this.shopCompleted = v; }
    public boolean isClaimCompleted() { return claimCompleted; }
    public void setClaimCompleted(boolean v) { this.claimCompleted = v; }
    public boolean isDuelCompleted() { return duelCompleted; }
    public void setDuelCompleted(boolean v) { this.duelCompleted = v; }
    public boolean isBountyCompleted() { return bountyCompleted; }
    public void setBountyCompleted(boolean v) { this.bountyCompleted = v; }
    public boolean isStockCompleted() { return stockCompleted; }
    public void setStockCompleted(boolean v) { this.stockCompleted = v; }
    public boolean isBankCompleted() { return bankCompleted; }
    public void setBankCompleted(boolean v) { this.bankCompleted = v; }
    public boolean isSubGuiOpen() { return subGuiOpen; }
    public void setSubGuiOpen(boolean v) { this.subGuiOpen = v; }

    public void addCompleted(String action) { completedActions.add(action); }
    public List<String> getCompleted() { return completedActions; }

    public boolean isStepDone(Step step) {
        return switch (step) {
            case SHOP -> shopCompleted;
            case CLAIM -> claimCompleted;
            case DUEL -> duelCompleted;
            case BOUNTY -> bountyCompleted;
            case STOCK -> stockCompleted;
            case BANK -> bankCompleted;
            case INTRO, HUB, COSMETICS, DONE -> true; // auto-completable
        };
    }
}
