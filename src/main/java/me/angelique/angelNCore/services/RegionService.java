package me.angelique.angelNCore.services;

import java.util.List;
import java.util.UUID;

public interface RegionService {
    enum RegionType { FERTILE, MINING, FUEL, CHOKEPOINT, DEFAULT }

    boolean claimChunk(UUID playerUUID, String world, int chunkX, int chunkZ, RegionType type);
    boolean releaseChunk(UUID playerUUID, String world, int chunkX, int chunkZ);
    UUID getChunkOwner(String world, int chunkX, int chunkZ);
    RegionType getRegionType(String world, int chunkX, int chunkZ);
    void setRegionType(String world, int chunkX, int chunkZ, RegionType type);
    int getClaimCount(UUID playerUUID);
    List<String> getPlayerClaims(UUID playerUUID);
    double getClaimTax(UUID playerUUID);
}
