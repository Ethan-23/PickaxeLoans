package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.model.Loan;

import java.util.List;
import java.util.UUID;

public interface LoanStorage {
    void init();
    void upsert(Loan loan);
    void delete(UUID loanUUID);
    List<Loan> loadAll();
    void close();
}
