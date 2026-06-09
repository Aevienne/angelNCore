package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.MarketManager;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.RegionService.RegionType;
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
        } else if (title.equals(ShopGui.DETAIL_TITLE)) {
            event.setCancelled(true);
            handleShopDetailClick(player, event);
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
            case 19 -> ShopGui.open(player, plugin, 0);
            case 20 -> player.performCommand("market");
            case 21 -> BankGui.open(player, plugin);
            case 22 -> StockGui.open(player, plugin, 0);
            case 23 -> player.performCommand("route");
            case 24 -> player.performCommand("company");
            case 25 -> player.performCommand("auction");
            case 28 -> player.performCommand("challenge");
            case 29 -> player.performCommand("wanted");
            case 30 -> player.performCommand("cosmetics");
            case 31 -> player.performCommand("factory");
            case 32 -> player.performCommand("season");
            case 33 -> player.performCommand("sustenance");
            case 34 -> ClaimGui.open(player, plugin);
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

        if (slot == 47) { player.closeInventory(); showPortfolio(player); return; }
        if (slot == 49) { player.closeInventory(); showToken(player); return; }
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

        if (slot == 49) { StockGui.open(player, plugin, stockPages.getOrDefault(id, 0)); return; }

        int[] buyAmounts = {1, 5, 10, 50, 100};
        for (int i = 0; i < buyAmounts.length; i++) {
            if (slot == 20 + i) {
                ex.placeOrder(id, company.companyId(), "buy", buyAmounts[i], company.currentPrice());
                player.sendMessage(TextUtil.color("&aBUY: " + buyAmounts[i] + " shares of " + company.name() + " @ $" + String.format("%.2f", company.currentPrice())));
                player.closeInventory();
                return;
            }
        }
        int[] sellAmounts = {1, 5, 10, 50, 100};
        for (int i = 0; i < sellAmounts.length; i++) {
            if (slot == 29 + i) {
                if (ex.getHolding(id, company.companyId()) < sellAmounts[i]) {
                    player.sendMessage(TextUtil.color("&cNot enough shares.")); return;
                }
                ex.placeOrder(id, company.companyId(), "sell", sellAmounts[i], company.currentPrice());
                player.sendMessage(TextUtil.color("&cSELL: " + sellAmounts[i] + " shares of " + company.name() + " @ $" + String.format("%.2f", company.currentPrice())));
                player.closeInventory();
                return;
            }
        }
    }

    // --- Claim ---
    private void handleClaimClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        RegionService rs = ServiceRegistry.getRegionService();
        var chunk = player.getLocation().getChunk();
        String world = chunk.getWorld().getName();
        int cx = chunk.getX(), cz = chunk.getZ();

        if (slot == 40) { AngelHubGui.open(player); return; }
        if (slot == 21) { doClaim(player, rs, world, cx, cz, null); return; }
        if (slot == 22) {
            if (event.isShiftClick()) {
                boolean ok = rs.releaseChunk(player.getUniqueId(), world, cx, cz);
                player.sendMessage(TextUtil.color(ok ? "&aChunk released." : "&cNot your chunk."));
            } else {
                doClaim(player, rs, world, cx, cz, RegionType.FERTILE);
            }
            return;
        }
        if (slot == 23) { doClaim(player, rs, world, cx, cz, RegionType.MINING); return; }
        if (slot == 24) { doClaim(player, rs, world, cx, cz, RegionType.FUEL); return; }
    }

    private void doClaim(Player player, RegionService rs, String world, int cx, int cz, RegionType type) {
        if (rs == null) return;
        if (rs.getChunkOwner(world, cx, cz) != null) {
            player.sendMessage(TextUtil.color("&cAlready claimed."));
            return;
        }
        int max = plugin.getConfig().getInt("land.max-claims", 10);
        if (rs.getClaimCount(player.getUniqueId()) >= max) {
            player.sendMessage(TextUtil.color("&cClaim limit reached (" + max + ")."));
            return;
        }
        double cost = plugin.getConfig().getDouble("land.claim-cost", 100.0);
        if (!plugin.getEconomyManager().has(player.getUniqueId(), cost)) {
            player.sendMessage(TextUtil.color("&cNeed $" + String.format("%.2f", cost)));
            return;
        }
        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost);
        rs.claimChunk(player.getUniqueId(), world, cx, cz, type != null ? type : RegionType.DEFAULT);
        player.sendMessage(TextUtil.color("&aChunk claimed! Type: " + (type != null ? type : "DEFAULT")));
        ClaimGui.open(player, plugin);
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
        if (market.isValidItem(itemKey)) {
            ShopGui.openDetail(player, plugin, itemKey);
        }
    }

    private void handleShopDetailClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        UUID id = player.getUniqueId();
        String itemKey = ShopGui.selectedItem.get(id);

        if (slot == 40) { ShopGui.open(player, plugin, shopPages.getOrDefault(id, 0)); return; }

        int[] buyAmounts = {1, 8, 16, 32, 64};
        for (int i = 0; i < buyAmounts.length; i++) {
            if (slot == 11 + i) { ShopGui.handleBuy(player, plugin, itemKey, buyAmounts[i]); ShopGui.openDetail(player, plugin, itemKey); return; }
        }
        int[] sellAmounts = {1, 8, 16, 32, 64};
        for (int i = 0; i < sellAmounts.length; i++) {
            if (slot == 29 + i) { ShopGui.handleSell(player, plugin, itemKey, sellAmounts[i]); ShopGui.openDetail(player, plugin, itemKey); return; }
        }
    }

    private int slotIndex(int slot) {
        int[] slots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) return i;
        return -1;
    }

    private void showPortfolio(Player player) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        if (ex == null) return;
        double total = 0;
        boolean hasAny = false;
        player.sendMessage(TextUtil.color("&6Your Portfolio:"));
        for (CompanyInfo c : ex.listCompanies()) {
            int shares = ex.getHolding(player.getUniqueId(), c.companyId());
            if (shares > 0) {
                double val = shares * c.currentPrice();
                total += val;
                hasAny = true;
                player.sendMessage(TextUtil.color("&e" + c.name() + "&f: " + shares + " shares &7(&a$" + String.format("%.2f", val) + "&7)"));
            }
        }
        if (!hasAny) player.sendMessage(TextUtil.color("&7No shares owned."));
        if (total > 0) player.sendMessage(TextUtil.color("&aTotal: &f$" + String.format("%.2f", total)));
    }

    private void showToken(Player player) {
        StockExchangeService ex = ServiceRegistry.getStockExchangeService();
        if (ex instanceof me.angelique.angelNCore.services.impl.StockExchangeServiceImpl se) {
            String token = se.generateToken(player.getUniqueId());
            if (!token.isEmpty()) {
                player.sendMessage(TextUtil.color("&aWeb token: &f" + token));
                player.sendMessage(TextUtil.color("&7Go to &bhttp://127.0.0.1:8080/app/"));
            } else {
                player.sendMessage(TextUtil.color("&cFailed to generate token."));
            }
        }
    }
}
