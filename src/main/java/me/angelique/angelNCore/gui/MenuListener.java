package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.economy.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuListener implements Listener {

    private final AngelNCore plugin;
    private final Map<UUID, Integer> shopPages = new HashMap<>();

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

    private void handleShopClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        UUID id = player.getUniqueId();
        int page = shopPages.getOrDefault(id, 0);
        MarketManager market = plugin.getMarketManager();
        Material type = clicked.getType();

        if (event.getSlot() == 48) {
            AngelHubGui.open(player);
            return;
        }

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

        if (isRight) {
            ShopGui.handleSell(player, plugin, itemKey, amount);
        } else {
            ShopGui.handleBuy(player, plugin, itemKey, amount);
        }

        // Reopen with refreshed prices
        new BukkitRunnable() {
            @Override
            public void run() {
                ShopGui.open(player, plugin, shopPages.getOrDefault(player.getUniqueId(), 0));
            }
        }.runTaskLater(plugin, 2L);
    }
}
