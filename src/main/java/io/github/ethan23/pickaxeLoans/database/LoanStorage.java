package io.github.ethan23.pickaxeLoans.database;

import java.util.List;
import java.util.UUID;

public interface LoanStorage {
    void init();
    void upsert(LoanRecord loanRecord);
    void delete(UUID loanUUID);
    List<LoanRecord> loadAll();
    void close();
}
