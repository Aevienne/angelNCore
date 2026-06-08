package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.events.EventBus;
import me.angelique.angelNCore.events.LandClaimChangedEvent;
import me.angelique.angelNCore.services.RegionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegionServiceImpl implements RegionService {

    private final AngelNCore plugin;

    public RegionServiceImpl(AngelNCore plugin) { this.plugin = plugin; }

    private Connection c() throws SQLException { return plugin.getDatabaseManager().getConnection(); }

    @Override
    public boolean claimChunk(UUID playerUUID, String world, int chunkX, int chunkZ, RegionType type) {
        try (PreparedStatement ps = c().prepareStatement(
                "INSERT INTO land_claims (world, chunk_x, chunk_z, owner_uuid, region_type) VALUES (?,?,?,?,?)")) {
            ps.setString(1, world);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            ps.setString(4, playerUUID.toString());
            ps.setString(5, type.name());
            ps.executeUpdate();
            EventBus.publish(new LandClaimChangedEvent(world + ":" + chunkX + ":" + chunkZ, playerUUID, "CLAIM"));
            return true;
        } catch (SQLException e) {
            return false; // duplicate or error
        }
    }

    @Override
    public boolean releaseChunk(UUID playerUUID, String world, int chunkX, int chunkZ) {
        try (PreparedStatement ps = c().prepareStatement(
                "DELETE FROM land_claims WHERE world=? AND chunk_x=? AND chunk_z=? AND owner_uuid=?")) {
            ps.setString(1, world);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            ps.setString(4, playerUUID.toString());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                EventBus.publish(new LandClaimChangedEvent(world + ":" + chunkX + ":" + chunkZ, playerUUID, "RELEASE"));
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("releaseChunk: " + e.getMessage());
        }
        return false;
    }

    @Override
    public UUID getChunkOwner(String world, int chunkX, int chunkZ) {
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT owner_uuid FROM land_claims WHERE world=? AND chunk_x=? AND chunk_z=?")) {
            ps.setString(1, world); ps.setInt(2, chunkX); ps.setInt(3, chunkZ);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return UUID.fromString(rs.getString(1));
        } catch (SQLException ignored) {}
        return null;
    }

    @Override
    public RegionType getRegionType(String world, int chunkX, int chunkZ) {
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT region_type FROM land_claims WHERE world=? AND chunk_x=? AND chunk_z=?")) {
            ps.setString(1, world); ps.setInt(2, chunkX); ps.setInt(3, chunkZ);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return RegionType.valueOf(rs.getString(1));
        } catch (SQLException ignored) {}
        return RegionType.DEFAULT;
    }

    @Override
    public void setRegionType(String world, int chunkX, int chunkZ, RegionType type) {
        try (PreparedStatement ps = c().prepareStatement(
                "UPDATE land_claims SET region_type=? WHERE world=? AND chunk_x=? AND chunk_z=?")) {
            ps.setString(1, type.name()); ps.setString(2, world); ps.setInt(3, chunkX); ps.setInt(4, chunkZ);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("setRegionType: " + e.getMessage());
        }
    }

    @Override
    public int getClaimCount(UUID playerUUID) {
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT COUNT(*) FROM land_claims WHERE owner_uuid=?")) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    @Override
    public List<String> getPlayerClaims(UUID playerUUID) {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT world, chunk_x, chunk_z, region_type FROM land_claims WHERE owner_uuid=?")) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1) + " " + rs.getInt(2) + " " + rs.getInt(3) + " [" + rs.getString(4) + "]");
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public double getClaimTax(UUID playerUUID) {
        int count = getClaimCount(playerUUID);
        return count * plugin.getConfig().getDouble("land.claim-tax-per-chunk", 10.0);
    }
}
