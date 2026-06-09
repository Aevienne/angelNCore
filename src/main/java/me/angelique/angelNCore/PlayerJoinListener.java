package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

public class PlayerJoinListener implements Listener {

    private final AngelNCore plugin;

    public PlayerJoinListener(AngelNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getEconomyManager().initPlayer(player);
        setupScoreboard(player);

        if (!player.hasPlayedBefore()) {
            giveStarterKit(player);
            showOnboarding(player);
        }
    }

    private void setupScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("angelhub", "dummy",
                ChatColor.translateAlternateColorCodes('&', "&6AngelNetwork"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 8;
        setLine(obj, line--, "&7");
        setLine(obj, line--, "&6Balance");
        setLine(obj, line--, "&f$" + String.format("%.2f", plugin.getEconomyManager().getBalance(player.getUniqueId())));
        setLine(obj, line--, "&8");
        setLine(obj, line--, "&7/menu for hub");
        setLine(obj, line--, "&7/shop to trade");
        setLine(obj, line--, "&0");
        setLine(obj, line--, "&6angelNCore");

        player.setScoreboard(board);
    }

    private void setLine(Objective obj, int score, String text) {
        Score s = obj.getScore(ChatColor.translateAlternateColorCodes('&', text));
        s.setScore(score);
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
            "&7A player-driven economy where every\n" +
            "&7action shapes the world.\n\n" +
            "&eOpen &f/tutorial &efor a guided tour\n" +
            "&eor &f/menu &eto get started\n" +
            "&7The hub has everything you need.\n\n" +
            "&aClaim land &7- stand in a chunk, open &e/claim\n" +
            "&aShop &7- buy & sell items at &e/shop\n" +
            "&aCompanies &7- form one at &e/company\n" +
            "&aDuels &7- fight for honor at &e/challenge\n" +
            "&aBounties &7- hunt wanted players at &e/wanted\n\n" +
            "&bWeb: &fhttp://127.0.0.1:8080/app/"
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
