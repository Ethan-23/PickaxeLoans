package io.github.ethan23.pickaxeLoans.service;

import io.github.ethan23.pickaxeLoans.model.Loan;

import java.util.*;

/**
 * In-memory repository that stores and indexes all active loan listings.
 *
 * <p>This repository maintains multiple indexes over the same loan data to
 * provide efficient lookups by loan ID, lender, borrower, and expiration time.
 * It also tracks loan listing expirations and active loan end times using
 * priority queues so the next event can be retrieved in constant time.
 *
 * <p>This class is responsible only for storing and indexing loan data. It
 * does not enforce business rules or perform loan state transitions.
 */
public class LoanRepository {

    /**
     * Maps each lender to the UUIDs of the loans they currently own.
     */
    private final HashMap<UUID, LinkedHashSet<UUID>> lenderToLoans = new HashMap<>();

    /**
     * Primary storage of loans keyed by their unique UUID.
     *
     * <p>A {@link LinkedHashMap} is used to preserve insertion order.
     */
    private final LinkedHashMap<UUID, Loan> loans = new LinkedHashMap<>();

    /**
     * Maps each borrower to the UUID of the loan they are currently borrowing.
     */
    private final HashMap<UUID, UUID> borrowerToLoan = new HashMap<>();

    /**
     * Orders loan listings by the time they expire.
     */
    private final PriorityQueue<Loan> expirationHeap =
            new PriorityQueue<>(Comparator.comparingLong(Loan::getListingExpiresAt));

    /**
     * Orders active loans by the time they are due to be returned.
     */
    private final PriorityQueue<Loan> endsAtHeap =
            new PriorityQueue<>(Comparator.comparingLong(
                    loan -> loan.getActiveLoan().getEndsAt()));

    /**
     * Adds a loan to the repository.
     *
     * <p>The loan is indexed by its UUID, associated with its lender, and
     * inserted into the listing expiration queue.
     *
     * @param loan the loan to add
     * @return {@code true} if the loan was added successfully;
     *         {@code false} if a loan with the same UUID already exists
     */
    public boolean add(Loan loan) {
        UUID loanUUID = loan.getLoanUUID();

        if (loans.containsKey(loanUUID)) {
            return false;
        }

        loans.put(loanUUID, loan);
        lenderToLoans
                .computeIfAbsent(loan.getLenderUUID(), k -> new LinkedHashSet<>())
                .add(loanUUID);

        expirationHeap.add(loan);

        return true;
    }

    /**
     * Updates repository indexes after a loan has been borrowed.
     *
     * <p>The borrower is associated with the loan and the loan is added to the
     * queue used to track return deadlines.
     *
     * @param loan the borrowed loan
     */
    public void recordBorrow(Loan loan) {
        borrowerToLoan.put(
                loan.getActiveLoan().getBorrowerUUID(),
                loan.getLoanUUID());

        endsAtHeap.add(loan);
    }

    /**
     * Updates repository indexes after a loan has been returned.
     *
     * <p>Removes the borrower association and the loan from the active loan
     * deadline queue.
     *
     * @param loan the returned loan
     */
    public void recordReturn(Loan loan) {
        borrowerToLoan.remove(loan.getActiveLoan().getBorrowerUUID());
        endsAtHeap.remove(loan);
    }

    /**
     * Removes a loan from the repository.
     *
     * <p>This removes the loan from the primary storage and the lender index.
     * It assumes the loan has already been removed from any other indexes if
     * necessary.
     *
     * @param loan the loan to remove
     */
    public void deleteLoan(Loan loan) {
        loans.remove(loan.getLoanUUID());

        LinkedHashSet<UUID> bucket = lenderToLoans.get(loan.getLenderUUID());

        if (bucket != null) {
            bucket.remove(loan.getLoanUUID());
        }
    }

    public Optional<Loan> findById(UUID loanUUID) {
        return Optional.ofNullable(loans.get(loanUUID));
    }

    public boolean isBorrowing(UUID borrowerUUID) {
        return borrowerToLoan.containsKey(borrowerUUID);
    }

    public Optional<UUID> getBorrowersLoanUUID(UUID uuid) {
        return Optional.ofNullable(borrowerToLoan.get(uuid));
    }

    /**
     * Returns an unmodifiable view of the loan UUIDs belonging to the given
     * lender.
     *
     * @param lenderUUID the lender to query
     * @return an unmodifiable set of loan UUIDs, or an empty set if the lender
     *         has no loans
     */
    public Set<UUID> getLoansByLender(UUID lenderUUID) {
        if (!lenderToLoans.containsKey(lenderUUID)) {
            return new HashSet<>(Set.of());
        }

        return Collections.unmodifiableSet(lenderToLoans.get(lenderUUID));
    }

    /**
     * Returns an unmodifiable view of all loans in the repository.
     */
    public Map<UUID, Loan> getLoans() {
        return Collections.unmodifiableMap(loans);
    }

    public Loan peekNextExpiration() {
        return expirationHeap.peek();
    }

    public Loan peekNextEndAt() {
        return endsAtHeap.peek();
    }

    public void removeTopEndAt() {
        endsAtHeap.poll();
    }

    public void removeTopExpiration() {
        expirationHeap.poll();
    }
}