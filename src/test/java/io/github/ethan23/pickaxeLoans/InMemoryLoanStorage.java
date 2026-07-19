package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.database.LoanStorage;
import io.github.ethan23.pickaxeLoans.model.Loan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryLoanStorage implements LoanStorage {

    final Map<UUID, Loan> saved = new LinkedHashMap<>();
    int upsertCount = 0;

    @Override
    public void init() {
    }

    @Override
    public void upsert(Loan loan) {
        saved.put(loan.getLoanUUID(), loan);
        upsertCount++;
    }

    @Override
    public void delete(UUID loanUUID) {
        saved.remove(loanUUID);
    }

    @Override
    public List<Loan> loadAll() {
        return new ArrayList<>(saved.values());
    }

    @Override
    public void close() {
    }
}
