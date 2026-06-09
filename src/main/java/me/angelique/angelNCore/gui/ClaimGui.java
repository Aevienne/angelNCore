package me.angelique.angelNCore.gui;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.RegionService;
import me.angelique.angelNCore.services.ServiceRegistry;
import me.angelique.angelNCore.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class ClaimGui {

    public static final String TITLE = TextUtil.color("&2Territory");
    static final int SIZE = 45;

    private ClaimGui() {}

    public static void open(Player player, AngelNCore plugin) {
        RegionService rs = ServiceRegistry.getRegionService();
        Chunk c = player.getLocation().getChunk();
        String world = c.getWorld().getName();
        int cx = c.getX(), cz = c.getZ();

        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);
        fillBorder(inv);

        inv.setItem(4, item(Material.OAK_FENCE_GATE, "&2Land Claims",
                "&7Chunk: &f" + cx + ", " + cz + " (" + world + ")"));

        // Current chunk info
        var owner = rs != null ? rs.getChunkOwner(world, cx, cz) : null;
        var type = rs != null ? rs.getRegionType(world, cx, cz) : RegionService.RegionType.DEFAULT;
        boolean owned = owner != null;
        boolean mine = owned && owner.equals(player.getUniqueId());

        inv.setItem(13, item(owned ? Material.GRASS_BLOCK : Material.DIRT,
                owned ? "&aClaimed" : "&7Unclaimed",
                "&7Type: &f" + type,
                "&7Owner: &f" + (owned ? owner.toString().substring(0, 8) : "None")));

        if (!owned) {
            inv.setItem(21, item(Material.EMERALD, "&aClaim This Chunk",
                    "&7Cost: $" + String.format("%.2f", plugin.getConfig().getDouble("land.claim-cost", 100.0)),
                    "&7Limit: " + (rs != null ? rs.getClaimCount(player.getUniqueId()) : 0) + "/" + plugin.getConfig().getInt("land.max-claims", 10),
                    "",
                    "&eClick to claim"));
            inv.setItem(22, item(Material.GOLD_INGOT, "&eClaim as FERTILE", "&7Best for farming"));
            inv.setItem(23, item(Material.IRON_PICKAXE, "&7Claim as MINING", "&7Best for mining"));
            inv.setItem(24, item(Material.COAL, "&8Claim as FUEL", "&7Best for energy"));
        } else if (mine) {
            inv.setItem(22, item(Material.BARRIER, "&cRelease Claim",
                    "&7Give up ownership of this chunk",
                    "",
                    "&eShift+Click to release"));
        } else {
            inv.setItem(22, item(Material.BARRIER, "&cClaimed by another player"));
        }

        // My claims list
        if (rs != null) {
            var claims = rs.getPlayerClaims(player.getUniqueId());
            inv.setItem(31, item(Material.BOOK, "&eMy Claims: &f" + claims.size(),
                    claims.stream().map(s -> "&7  " + s).toArray(String[]::new)));
        }

        inv.setItem(40, item(Material.OAK_DOOR, "&cBack to Hub"));

        player.openInventory(inv);
    }

    static void fillBorder(Inventory inv) {
        ItemStack glass = pane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) inv.setItem(i, glass);
    }

    static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.color(name));
            meta.setLore(Arrays.stream(lore).map(TextUtil::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    static ItemStack pane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }
}
