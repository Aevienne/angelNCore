package me.angelique.angelNCore.economy;

import me.angelique.angelNCore.AngelNCore;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconomyBridge implements Economy {

    private final EconomyManager manager;

    public VaultEconomyBridge(EconomyManager manager) { this.manager = manager; }

    @Override public boolean isEnabled() { return true; }
    @Override public String getName() { return "angelNCore"; }
    @Override public String currencyNamePlural() { return "Coins"; }
    @Override public String currencyNameSingular() { return "Coin"; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String format(double amount) { return manager.formatBalance(amount); }
    @Override public boolean hasBankSupport() { return false; }

    // Bank (not supported)
    @Override public EconomyResponse createBank(String s, String s1) { return fail("No bank support"); }
    @Override public EconomyResponse createBank(String s, OfflinePlayer op) { return fail("No bank support"); }
    @Override public EconomyResponse deleteBank(String s) { return fail("No bank support"); }
    @Override public EconomyResponse bankBalance(String s) { return fail("No bank support"); }
    @Override public EconomyResponse bankHas(String s, double v) { return fail("No bank support"); }
    @Override public EconomyResponse bankWithdraw(String s, double v) { return fail("No bank support"); }
    @Override public EconomyResponse bankDeposit(String s, double v) { return fail("No bank support"); }
    @Override public EconomyResponse isBankOwner(String s, String s1) { return fail("No bank support"); }
    @Override public EconomyResponse isBankOwner(String s, OfflinePlayer op) { return fail("No bank support"); }
    @Override public EconomyResponse isBankMember(String s, String s1) { return fail("No bank support"); }
    @Override public EconomyResponse isBankMember(String s, OfflinePlayer op) { return fail("No bank support"); }
    @Override public List<String> getBanks() { return List.of(); }

    // Accounts
    @Override public boolean hasAccount(String s) { return true; }
    @Override public boolean hasAccount(OfflinePlayer op) { return true; }
    @Override public boolean hasAccount(String s, String s1) { return true; }
    @Override public boolean hasAccount(OfflinePlayer op, String s) { return true; }

    // Balance
    @Override public double getBalance(String s) { return 0; }
    @Override public double getBalance(OfflinePlayer op) { return manager.getBalance(op.getUniqueId()); }
    @Override public double getBalance(String s, String s1) { return 0; }
    @Override public double getBalance(OfflinePlayer op, String s) { return manager.getBalance(op.getUniqueId()); }

    // Has
    @Override public boolean has(String s, double v) { return false; }
    @Override public boolean has(OfflinePlayer op, double v) { return manager.has(op.getUniqueId(), v); }
    @Override public boolean has(String s, String s1, double v) { return false; }
    @Override public boolean has(OfflinePlayer op, String s, double v) { return manager.has(op.getUniqueId(), v); }

    // Withdraw
    @Override public EconomyResponse withdrawPlayer(String s, double v) { return fail("Use UUID"); }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer op, double v) {
        return manager.withdraw(op.getUniqueId(), v)
            ? new EconomyResponse(v, manager.getBalance(op.getUniqueId()), EconomyResponse.ResponseType.SUCCESS, "")
            : new EconomyResponse(0, manager.getBalance(op.getUniqueId()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
    }
    @Override public EconomyResponse withdrawPlayer(String s, String s1, double v) { return fail("Use UUID"); }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer op, String s, double v) { return withdrawPlayer(op, v); }

    // Deposit
    @Override public EconomyResponse depositPlayer(String s, double v) { return fail("Use UUID"); }
    @Override public EconomyResponse depositPlayer(OfflinePlayer op, double v) {
        manager.deposit(op.getUniqueId(), v);
        return new EconomyResponse(v, manager.getBalance(op.getUniqueId()), EconomyResponse.ResponseType.SUCCESS, "");
    }
    @Override public EconomyResponse depositPlayer(String s, String s1, double v) { return fail("Use UUID"); }
    @Override public EconomyResponse depositPlayer(OfflinePlayer op, String s, double v) { return depositPlayer(op, v); }

    // Create account
    @Override public boolean createPlayerAccount(String s) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer op) { return true; }
    @Override public boolean createPlayerAccount(String s, String s1) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer op, String s) { return true; }

    private EconomyResponse fail(String msg) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, msg);
    }
}
