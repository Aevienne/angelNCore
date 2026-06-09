package me.angelique.angelNCore.services.impl;

import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.BankService;
import me.angelique.angelNCore.services.StockExchangeService;
import me.angelique.angelNCore.services.ServiceRegistry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankServiceImpl implements BankService {

    private final AngelNCore plugin;

    public BankServiceImpl(AngelNCore plugin) {
        this.plugin = plugin;
    }

    private Connection c() throws SQLException { return plugin.getDatabaseManager().getConnection(); }

    @Override
    public String createLoan(UUID borrower, double amount, double rate, int termDays) {
        if (borrower == null || amount <= 0) return "";
        // Credit check: reject if borrower has defaulted loans
        List<LoanInfo> existing = getLoans(borrower);
        for (LoanInfo l : existing) {
            if ("defaulted".equals(l.status())) {
                plugin.getLogger().warning("Loan rejected: " + borrower + " has defaulted loan " + l.loanId());
                return "";
            }
        }
        // Limit: max 3 active loans
        long activeCount = existing.stream().filter(l -> "active".equals(l.status())).count();
        if (activeCount >= 3) {
            plugin.getLogger().warning("Loan rejected: " + borrower + " already has 3 active loans");
            return "";
        }
        // Collateral check: player must have a company with treasury >= loan amount
        CompanyServiceImpl cs = (CompanyServiceImpl) ServiceRegistry.getCompanyService();
        if (cs != null) {
            var company = cs.getCompanyByOwner(borrower);
            if (company == null) {
                plugin.getLogger().warning("Loan rejected: " + borrower + " has no company as collateral");
                return "";
            }
            if (company.treasury() < amount * 0.5) {
                plugin.getLogger().warning("Loan rejected: " + borrower + " company treasury too low (" + String.format("%.2f", company.treasury()) + " < " + String.format("%.2f", amount * 0.5) + ")");
                return "";
            }
        }
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long due = now + (long) termDays * 86400000L;
        try (PreparedStatement ps = c().prepareStatement(
                "INSERT INTO loans (loan_id, borrower_uuid, amount, remaining, rate, issued_at, due_at, status) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, borrower.toString());
            ps.setDouble(3, amount);
            ps.setDouble(4, amount);
            ps.setDouble(5, rate);
            ps.setLong(6, now);
            ps.setLong(7, due);
            ps.setString(8, "active");
            ps.executeUpdate();
            plugin.getEconomyManager().deposit(borrower, amount);
        } catch (SQLException e) {
            plugin.getLogger().warning("createLoan: " + e.getMessage());
        }
        return id;
    }

    @Override
    public boolean repayLoan(String loanId, UUID payer, double amount) {
        try (PreparedStatement ps = c().prepareStatement("SELECT remaining, borrower_uuid, status FROM loans WHERE loan_id=?")) {
            ps.setString(1, loanId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            double remaining = rs.getDouble(1);
            String borrower = rs.getString(2);
            String status = rs.getString(3);
            if (!"active".equals(status)) return false;

            double toRepay = Math.min(amount, remaining);
            if (plugin.getEconomyManager().withdraw(payer, toRepay)) {
                double newRemaining = remaining - toRepay;
                String newStatus = newRemaining <= 0.01 ? "repaid" : "active";
                try (PreparedStatement u = c().prepareStatement("UPDATE loans SET remaining=?, status=? WHERE loan_id=?")) {
                    u.setDouble(1, newRemaining);
                    u.setString(2, newStatus);
                    u.setString(3, loanId);
                    u.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("repayLoan: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<LoanInfo> getLoans(UUID borrower) {
        List<LoanInfo> list = new ArrayList<>();
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT loan_id, borrower_uuid, amount, remaining, rate, issued_at, due_at, status FROM loans WHERE borrower_uuid=?")) {
            ps.setString(1, borrower.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LoanInfo(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getLong(6), rs.getLong(7), rs.getString(8)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getLoans: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean processInterest(String loanId) {
        try {
            try (PreparedStatement ps = c().prepareStatement("SELECT remaining, rate FROM loans WHERE loan_id=? AND status='active'")) {
                ps.setString(1, loanId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false;
                double remaining = rs.getDouble(1);
                double rate = rs.getDouble(2);
                double interest = remaining * rate / 365.0;
                try (PreparedStatement u = c().prepareStatement("UPDATE loans SET remaining=remaining+? WHERE loan_id=?")) {
                    u.setDouble(1, interest);
                    u.setString(2, loanId);
                    u.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("processInterest: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<LoanInfo> getDefaultedLoans() {
        List<LoanInfo> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        try (Statement stmt = c().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT loan_id, borrower_uuid, amount, remaining, rate, issued_at, due_at, status FROM loans WHERE status='active' AND due_at < " + now)) {
            while (rs.next()) {
                list.add(new LoanInfo(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getLong(6), rs.getLong(7), rs.getString(8)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getDefaultedLoans: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<LoanInfo> getActiveLoans() {
        List<LoanInfo> list = new ArrayList<>();
        try (Statement stmt = c().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT loan_id, borrower_uuid, amount, remaining, rate, issued_at, due_at, status FROM loans WHERE status='active'")) {
            while (rs.next()) {
                list.add(new LoanInfo(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getLong(6), rs.getLong(7), rs.getString(8)));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("getActiveLoans: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void chargeMaintenance(String companyId, double amount) {
        CompanyServiceImpl cs = (CompanyServiceImpl) ServiceRegistry.getCompanyService();
        if (cs == null) return;
        if (cs.getBalance(companyId) >= amount) {
            cs.updateBalance(companyId, -amount);
        } else {
            // Can't pay — trigger liquidation
            liquidateCompany(companyId);
        }
    }

    @Override
    public void liquidateCompany(String companyId) {
        StockExchangeService sx = ServiceRegistry.getStockExchangeService();
        if (sx != null && sx.isListed(companyId)) {
            sx.applyDamageModifier(companyId, 0.50); // halve the stock value
        }
        plugin.getLogger().warning("Company " + companyId + " bankrupt — liquidation triggered.");
        try (PreparedStatement ps = c().prepareStatement("UPDATE loans SET status='defaulted' WHERE borrower_uuid=? AND status='active'")) {
            ps.setString(1, companyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("liquidateCompany: " + e.getMessage());
        }
    }
}
