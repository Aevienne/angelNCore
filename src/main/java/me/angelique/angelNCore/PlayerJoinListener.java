package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final AngelNCore plugin;

    public PlayerJoinListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getEconomyManager().initPlayer(player);

        if (!player.hasPlayedBefore()) {
            giveStarterKit(player);
            showOnboarding(player);
        }
    }

    private void giveStarterKit(Player player) {
        if (!plugin.getConfig().getBoolean("onboarding.starter-kit", true)) return;
        player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
        player.getInventory().addItem(new ItemStack(Material.STONE_AXE));
        player.getInventory().addItem(new ItemStack(Material.STONE_SHOVEL));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 8));
        player.getInventory().addItem(new ItemStack(Material.OAK_LOG, 4));
    }

    private void showOnboarding(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&6================================\n" +
            "&6  Welcome to &eAngelNetwork&6!\n" +
            "&6================================\n" +
            "&7You are entering a player-driven economy.\n" +
            "&7Every action affects supply, demand, and prices.\n\n" +
            "&e6 Roles to play:\n" +
            "  &aProducer &7- Farm, mine, gather raw goods\n" +
            "  &6Industrialist &7- Run factories, process materials\n" +
            "  &bLogistics &7- Transport goods between regions\n" +
            "  &cMercenary &7- Bounty hunting, escort contracts\n" +
            "  &5Regional Power &7- Control land, tax production\n" +
            "  &dFinancial &7- Trade stocks, lend currency\n\n" +
            "&7Commands: &e/shop &7| &e/balance &7| &e/claim &7| &e/contract\n" +
            "&aYou have a free land claim! Use &e/claim claim &ato start."
        ));

        // Grant free claim
        int free = plugin.getConfig().getInt("onboarding.free-claims", 1);
        if (free > 0) {
            RegionService rs = ServiceRegistry.getRegionService();
            if (rs != null) {
                player.sendMessage(ChatColor.GREEN + "You have " + free + " free land claim(s). Stand in a chunk and type /claim claim.");
            }
        }
    }
}
