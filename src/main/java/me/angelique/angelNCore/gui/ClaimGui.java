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
                "&7Standing in chunk &f" + cx + ", " + cz));

        var owner = rs != null ? rs.getChunkOwner(world, cx, cz) : null;
        boolean owned = owner != null;
        boolean mine = owned && owner.equals(player.getUniqueId());

        if (!owned) {
            double cost = plugin.getConfig().getDouble("land.claim-cost", 100.0);
            int myClaims = rs != null ? rs.getClaimCount(player.getUniqueId()) : 0;
            int max = plugin.getConfig().getInt("land.max-claims", 10);

            inv.setItem(13, item(Material.GRASS_BLOCK, "&aAvailable",
                    "&7Cost: &f$" + String.format("%.2f", cost),
                    "&7Your claims: &f" + myClaims + " / " + max,
                    "",
                    "&eClick Claim to take this chunk"));

            inv.setItem(22, item(Material.EMERALD, "&aClaim",
                    "&7Pay $" + String.format("%.2f", cost),
                    "&7to own this chunk",
                    "",
                    "&eClick to claim"));
        } else if (mine) {
            inv.setItem(13, item(Material.GRASS_BLOCK, "&aYour Territory",
                    "&7You own this chunk"));

            inv.setItem(22, item(Material.BARRIER, "&cRelease",
                    "&7Give up ownership",
                    "",
                    "&eClick to release"));
        } else {
            inv.setItem(13, item(Material.BEDROCK, "&cClaimed",
                    "&7Owned by: &f" + owner.toString().substring(0, 8)));
        }

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
