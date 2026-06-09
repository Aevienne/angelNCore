package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.StockExchangeService.CompanyInfo;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MenuListener implements Listener {

    private final AngelNCore plugin;
    private final Map<UUID, Integer> shopPages = new HashMap<>();
    private final Map<UUID, Integer> stockPages = new HashMap<>();

    public MenuListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(AngelHubGui.TITLE)) {
            event.setCancelled(true);
            handleHubClick(player, event.getSlot());
        } else if (title.equals(ShopGui.TITLE)) {
            event.setCancelled(true);
            handleShopClick(player, event);
        } else if (title.equals(BankGui.TITLE)) {
            event.setCancelled(true);
            handleBankClick(player, event);
        } else if (title.equals(StockGui.TITLE)) {
            event.setCancelled(true);
            handleStockClick(player, event);
        } else if (title.equals(StockGui.TRADE_TITLE)) {
            event.setCancelled(true);
            handleTradeClick(player, event);
        } else if (title.equals(ClaimGui.TITLE)) {
            event.setCancelled(true);
            handleClaimClick(player, event);
        }
    }

    private void handleHubClick(Player player, int slot) {
        switch (slot) {
            case 19 -> player.performCommand("shop");
            case 20 -> player.performCommand("market");
            case 21 -> player.performCommand("bank");
            case 22 -> player.performCommand("stock");
            case 23 -> player.performCommand("route");
            case 24 -> player.performCommand("company");
            case 25 -> player.performCommand("auction");
            case 28 -> player.performCommand("challenge");
            case 29 -> player.performCommand("wanted");
            case 30 -> player.performCommand("cosmetics");
            case 31 -> player.performCommand("factory");
            case 32 -> player.performCommand("season");
            case 33 -> player.performCommand("sustenance");
            case 34 -> player.performCommand("claim");
            case 49 -> player.closeInventory();
        }
    }

    // --- Bank ---
    private void handleBankClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        UUID id = player.getUniqueId();

        if (slot == 40) { AngelHubGui.open(player); return; }

        // Loan repay: clicking on active loan slots (19-25)
        List<BankService.LoanInfo> loans = BankGui.activeLoanCache.get(id);
        if (loans != null) {
            for (int i = 0; i < Math.min(loans.size(), 7); i++) {
                if (slot == 19 + i) {
                    BankService.LoanInfo l = loans.get(i);
                    BankService bank = ServiceRegistry.getBankService();
                    double bal = plugin.getEconomyManager().getBalance(id);
                    double toPay = Math.min(bal, l.remaining());
                    if (toPay <= 0) {
                        player.sendMessage(TextUtil.color("&cInsufficient balance to repay."));
                    } else if (bank != null && bank.repayLoan(l.loanId(), id, toPay)) {
                        player.sendMessage(TextUtil.color("&aRepaid $" + String.format("%.2f", toPay) + " on loan #" + l.loanId().substring(0, 8)));
                    } else {
                        player.sendMessage(TextUtil.color("&cRepayment failed."));
                    }
                    BankGui.activeLoanCache.remove(id);
                    BankGui.open(player, plugin);
                    return;
                }
            }
        }

        // Borrow: clicking on borrow slots (28-34)
        for (int i = 0; i < 7; i++) {
            if (slot == 28 + i) {
                double amount = new double[]{1000, 5000, 10000, 30000, 50000, 100000, 250000}[i];
                int term = new int[]{5, 10, 15, 30, 30, 60, 90}[i];
                BankService bank = ServiceRegistry.getBankService();
                double rate = plugin.getConfig().getDouble("bank.default-rate", 0.05);
                if (bank == null) { player.sendMessage(TextUtil.color("&cBank unavailable.")); return; }
                String loanId = bank.createLoan(id, amount, rate, term);
                if (loanId == null || loanId.isEmpty()) {
                    player.sendMessage(TextUtil.color("&cLoan rejected. Need company with treasury \u2265 $" + String.format("%.0f", amount * 0.5)));
                } else {
                    player.sendMessage(TextUtil.color("&aBorrowed $" + String.format("%.0f", amount) + " for " + term + " days"));
                }
                BankGui.activeLoanCache.remove(id);
                BankGui.open(player, plugin);
                return;
            }
        }
    }

    // --- Stock ---
    private void handleStockClick(Player player, InventoryClickEvent event) {
        UUID id = player.getUniqueId();
        int slot = event.getSlot();

        if (slot == 47) { player.closeInventory(); player.chat("/stock portfolio"); return; }
        if (slot == 49) { player.closeInventory(); player.chat("/stock token"); return; }
        if (slot == 51) { AngelHubGui.open(player); return; }
        if (slot == 45) { stockPages.put(id, Math.max(0, stockPages.getOrDefault(id, 0) - 1)); StockGui.open(player, plugin, stockPages.get(id)); return; }
        if (slot == 53) { stockPages.put(id, stockPages.getOrDefault(id, 0) + 1); StockGui.open(player, plugin, stockPages.get(id)); return; }

        // Company item clicked
        List<CompanyInfo> companies = StockGui.companyCache.get(id);
        if (companies == null) return;
        int perPage = 28;
        int page = stockPages.getOrDefault(id, 0);
        int start = page * perPage;
        int slotIdx = slotIndex(slot);
        if (slotIdx < 0) return;
        int companyIdx = start + slotIdx;
        if (companyIdx < companies.size()) {
            StockGui.openTrade(player, plugin, companies.get(companyIdx));
        }
    }

    private void handleTradeClick(Player player, InventoryClickEvent event) {
        UUID id = player.getUniqueId();
        int slot = event.getSlot();
        CompanyInfo company = StockGui.tradeSelection.get(id);
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        if (company == null || ex == null) return;

        if (slot == 31) { StockGui.open(player, plugin, stockPages.getOrDefault(id, 0)); return; }

        // Buy amounts (slots 10-14)
        int[] buyAmounts = {1, 5, 10, 50, 100};
        for (int i = 0; i < buyAmounts.length; i++) {
            if (slot == 10 + i) {
                String orderId = ex.placeOrder(id, company.companyId(), "buy", buyAmounts[i], company.currentPrice());
                player.sendMessage(TextUtil.color("&aBUY order placed: " + buyAmounts[i] + " shares of " + company.name() + " @ $" + String.format("%.2f", company.currentPrice())));
                player.closeInventory();
                return;
            }
        }

        // Sell amounts (slots 20-24)
        int[] sellAmounts = {1, 5, 10, 50, 100};
        for (int i = 0; i < sellAmounts.length; i++) {
            if (slot == 20 + i) {
                int myShares = ex.getHolding(id, company.companyId());
                if (myShares < sellAmounts[i]) {
                    player.sendMessage(TextUtil.color("&cYou only have " + myShares + " shares."));
                    return;
                }
                String orderId = ex.placeOrder(id, company.companyId(), "sell", sellAmounts[i], company.currentPrice());
                player.sendMessage(TextUtil.color("&cSELL order placed: " + sellAmounts[i] + " shares of " + company.name() + " @ $" + String.format("%.2f", company.currentPrice())));
                player.closeInventory();
                return;
            }
        }
    }

    // --- Claim ---
    private void handleClaimClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 21) { player.closeInventory(); player.chat("/claim claim"); }
        else if (slot == 22) {
            if (event.isShiftClick()) { player.closeInventory(); player.chat("/claim release"); }
            else { player.closeInventory(); player.chat("/claim claim FERTILE"); }
        }
        else if (slot == 23) { player.closeInventory(); player.chat("/claim claim MINING"); }
        else if (slot == 24) { player.closeInventory(); player.chat("/claim claim FUEL"); }
        else if (slot == 40) { AngelHubGui.open(player); }
    }

    // --- Shop ---
    private void handleShopClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        UUID id = player.getUniqueId();
        int page = shopPages.getOrDefault(id, 0);
        MarketManager market = plugin.getMarketManager();
        Material type = clicked.getType();

        if (event.getSlot() == 48) { AngelHubGui.open(player); return; }
        if (event.getSlot() == 45 && type == Material.ARROW) {
            shopPages.put(id, Math.max(0, page - 1));
            ShopGui.open(player, plugin, page - 1);
            return;
        }
        if (event.getSlot() == 53 && type == Material.ARROW) {
            shopPages.put(id, page + 1);
            ShopGui.open(player, plugin, page + 1);
            return;
        }

        String itemKey = type.name();
        if (!market.isValidItem(itemKey)) return;

        boolean isShift = event.isShiftClick();
        boolean isRight = event.isRightClick();
        int amount = isShift ? 64 : 1;

        if (isRight) ShopGui.handleSell(player, plugin, itemKey, amount);
        else ShopGui.handleBuy(player, plugin, itemKey, amount);

        new BukkitRunnable() {
            @Override
            public void run() { ShopGui.open(player, plugin, shopPages.getOrDefault(player.getUniqueId(), 0)); }
        }.runTaskLater(plugin, 2L);
    }

    private int slotIndex(int slot) {
        int[] slots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) return i;
        return -1;
    }
}
