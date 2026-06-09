package me.angelique.angelNCore.services;

import java.util.List;
import java.util.UUID;

public interface CompanyService {
    String createCompany(String name, UUID founder);
    void addMember(String companyId, UUID playerId);
    void removeMember(String companyId, UUID playerId);
    List<UUID> getMembers(String companyId);
    double getBalance(String companyId);
    void updateBalance(String companyId, double delta);
    String getOwner(String companyId);
    boolean exists(String companyId);

    /** Returns the company ID the given player belongs to, or null if they have none. */
    String getCompanyForPlayer(UUID playerId);

    record CompanyInfo(String id, String name, UUID owner, double treasury) {}

    CompanyInfo getCompanyByOwner(UUID ownerId);
}
