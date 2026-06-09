package me.angelique.angelNCore.services;

import java.util.List;
import java.util.UUID;

public interface BankService {
    record LoanInfo(String loanId, String borrowerUUID, double amount, double remaining, double rate, long issuedAt, long dueAt, String status) {}

    String createLoan(UUID borrower, double amount, double rate, int termDays);
    boolean repayLoan(String loanId, UUID payer, double amount);
    List<LoanInfo> getLoans(UUID borrower);
    boolean processInterest(String loanId);
    List<LoanInfo> getDefaultedLoans();
    List<LoanInfo> getActiveLoans();

    // Bankruptcy
    void chargeMaintenance(String companyId, double amount);
    void liquidateCompany(String companyId);
}
