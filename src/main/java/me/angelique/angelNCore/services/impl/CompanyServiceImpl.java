package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.services.CompanyService;
import java.util.*;

public class CompanyServiceImpl implements CompanyService {
    private final Map<String, CompanyData> companies = new HashMap<>();

    @Override
    public String createCompany(String name, UUID founder) {
        String id = UUID.randomUUID().toString();
        companies.put(id, new CompanyData(name, founder));
        return id;
    }

    @Override
    public void addMember(String companyId, UUID playerId) {
        CompanyData data = companies.get(companyId);
        if (data != null) {
            data.members.add(playerId);
        }
    }

    @Override
    public void removeMember(String companyId, UUID playerId) {
        CompanyData data = companies.get(companyId);
        if (data != null) {
            data.members.remove(playerId);
        }
    }

    @Override
    public List<UUID> getMembers(String companyId) {
        CompanyData data = companies.get(companyId);
        return data != null ? new ArrayList<>(data.members) : new ArrayList<>();
    }

    @Override
    public double getBalance(String companyId) {
        CompanyData data = companies.get(companyId);
        return data != null ? data.balance : 0;
    }

    @Override
    public void updateBalance(String companyId, double delta) {
        CompanyData data = companies.get(companyId);
        if (data != null) {
            data.balance += delta;
        }
    }

    @Override
    public String getOwner(String companyId) {
        CompanyData data = companies.get(companyId);
        return data != null ? data.founder.toString() : null;
    }

    @Override
    public boolean exists(String companyId) {
        return companies.containsKey(companyId);
    }

    @Override
    public String getCompanyForPlayer(UUID playerId) {
        for (Map.Entry<String, CompanyData> entry : companies.entrySet()) {
            if (entry.getValue().members.contains(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public CompanyInfo getCompanyByOwner(UUID ownerId) {
        for (Map.Entry<String, CompanyData> entry : companies.entrySet()) {
            if (entry.getValue().founder.equals(ownerId)) {
                CompanyData d = entry.getValue();
                return new CompanyInfo(entry.getKey(), d.name, d.founder, d.balance);
            }
        }
        return null;
    }

    private static class CompanyData {
        String name;
        UUID founder;
        Set<UUID> members = new HashSet<>();
        double balance = 0;

        CompanyData(String name, UUID founder) {
            this.name = name;
            this.founder = founder;
            this.members.add(founder);
        }
    }
}
