package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.model.Loan;

import java.util.List;
import java.util.UUID;

/**
 * Persistence boundary for loans.
 *
 * <p>The in-memory repository is the source of truth at runtime;
 * implementations only need to keep a durable copy that {@link #loadAll()}
 * can rebuild the repository from after a restart.
 *
 * <p>All methods are invoked on the server main thread. Implementations
 * should fail loudly rather than corrupt or silently drop data.
 */
public interface LoanStorage {

    /**
     * Opens the underlying storage and creates the schema if it does not
     * exist yet.
     */
    void init();

    /**
     * Saves the loan's current state, inserting it or replacing the stored
     * copy if one already exists.
     *
     * @param loan the loan to save
     */
    void upsert(Loan loan);

    /**
     * Permanently removes the loan. Does nothing if it is not stored.
     *
     * @param loanUUID the id of the loan to remove
     */
    void delete(UUID loanUUID);

    /**
     * Loads every stored loan in creation order.
     *
     * @return all stored loans, oldest first
     */
    List<Loan> loadAll();

    /** Releases the underlying storage. Safe to call even if init failed. */
    void close();
}
