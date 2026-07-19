package io.github.ethan23.pickaxeLoans.service;

import io.github.ethan23.pickaxeLoans.database.LoanStorage;
import io.github.ethan23.pickaxeLoans.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coordinates all loan-related business operations.
 *
 * <p>This service validates loan operations, updates repository indexes,
 * persists loan state, and manages loan lifecycle events such as creating,
 * borrowing, returning, cancelling, and expiring loans.
 *
 * <p>Frequently changing loan data, such as accrued taxes, is batched and
 * periodically written to persistent storage to reduce database operations.
 */
public class LoanService {

    private final LoanRepository repository;
    private final LoanStorage loanStorage;
    private final Set<UUID> dirtyLoans;
    private final Logger logger;

    /** Number of decimal places used when converting percentage values. */
    private static final int DECIMAL_MOVEMENT = 2;

    /** Maximum number of active listings a lender may have. */
    private static final int MAX_LOAN_COUNT = 3;


    public LoanService(LoanRepository repository, LoanStorage loanStorage, Logger logger) {
        this.repository = repository;
        this.loanStorage = loanStorage;
        this.logger = logger;
        this.dirtyLoans = new HashSet<>();
    }

    /**
     * Creates a new loan listing.
     *
     * <p>The lender may have at most {@value #MAX_LOAN_COUNT} active listings.
     * On success, the loan is added to the repository and persisted.
     *
     * @param loan the loan to create
     * @return the result of the creation attempt
     */
    public LoanResult createListing(Loan loan) {
        if(repository.getLoansByLender(loan.getLenderUUID()).size() >= MAX_LOAN_COUNT){
            return LoanResult.MAX_LOANS;
        }

        if (!repository.add(loan)) {
            return LoanResult.DUPLICATE_LOAN;
        }
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    /**
     * Cancels a listed loan.
     *
     * <p>Only loans currently in the {@link LoanState#LISTED} state may be
     * canceled.
     *
     * @param loanUUID the loan to cancel
     * @return the outcome of the cancellation
     */
    public LoanResult cancel(UUID loanUUID) {
        Loan loan = repository.findById(loanUUID).orElse(null);

        if (loan == null){
            return LoanResult.NOT_FOUND;
        }

        if (loan.getLoanState() != LoanState.LISTED){
            return LoanResult.NOT_LISTED;
        }

        loan.cancel();
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    /**
     * Borrows a listed loan.
     *
     * <p>Validates that the borrower is eligible to borrow the loan before
     * marking it as borrowed, updating repository indexes, and persisting
     * the change.
     *
     * @param borrowerUUID the player borrowing the loan
     * @param loanUUID the loan to borrow
     * @return the outcome of the borrow operation
     */
    public LoanResult borrow(UUID borrowerUUID, UUID loanUUID) {
        if (repository.isBorrowing(borrowerUUID)){
            return LoanResult.ALREADY_BORROWING;
        }

        Loan loan = repository.findById(loanUUID).orElse(null);
        if (loan == null){
            return LoanResult.NOT_FOUND;
        }

        if(borrowerUUID.equals(loan.getLenderUUID())){
            return LoanResult.LENDERS_LOAN;
        }

        if (loan.getLoanState() != LoanState.LISTED){
            return LoanResult.NOT_LISTED;
        }

        loan.markBorrowed(new ActiveLoan(borrowerUUID, loan.getLoanDeal().getLoanDurationMillis()));
        repository.recordBorrow(loan);
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    /**
     * Marks a listed loan as expired.
     *
     * <p>Expired loans can no longer be borrowed.
     *
     * @param loan the loan to expire
     * @return the outcome of the expiration
     */
    public LoanResult expire(Loan loan){

        if(loan.getLoanState() != LoanState.LISTED){
            return LoanResult.NOT_LISTED;
        }
        loan.expire();
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    /**
     * Permanently removes a loan from the repository and persistent storage.
     *
     * @param loan the loan to delete
     */
    public void deleteLoan(Loan loan){
        repository.deleteLoan(loan);
        loanStorage.delete(loan.getLoanUUID());
    }

    /**
     * Returns an active loan.
     *
     * <p>The loan is marked as returned, repository indexes are updated,
     * and the change is persisted.
     *
     * @param loanUUID the loan to return
     * @return the outcome of the return operation
     */
    public LoanResult returnLoan(UUID loanUUID) {
        Loan loan = repository.findById(loanUUID).orElse(null);
        if (loan == null){
            return LoanResult.NOT_FOUND;
        }
        if (loan.getLoanState() != LoanState.BORROWED){
            return LoanResult.NOT_BORROWED;
        }

        loan.markReturned();
        repository.recordReturn(loan);
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }


    public List<Loan> getListedLoans(){
        List<Loan> listedLoans = new ArrayList<>();

        List<Loan> loans = new ArrayList<>(repository.getLoans().values());

        for(Loan loan : loans){
            if(loan.getLoanState() == LoanState.LISTED){
                listedLoans.add(loan);
            }
        }

        return(listedLoans);
    }

    public List<Loan> getLenderLoans(UUID lenderUUID){

        List<UUID> loanUUIDs = new ArrayList<>(repository.getLoansByLender(lenderUUID));

        List<Loan> loans = new ArrayList<>();

        for(UUID uuid : loanUUIDs){
            Loan loan = repository.findById(uuid).orElse(null);
            if(loan == null){
                continue;
            }
            loans.add(loan);
        }

        return loans;
    }

    public Loan getBorrowersLoan(UUID borrowerUUID) {
        Optional<UUID> loanUUID = repository.getBorrowersLoanUUID(borrowerUUID);

        if(loanUUID.isEmpty()){
            return null;
        }

        Optional<Loan> loan = repository.findById(loanUUID.get());

        return loan.orElse(null);

    }

    public boolean isBorrower(UUID uuid) {
        return repository.isBorrowing(uuid);
    }

    /**
     * Processes all listed loans whose listing duration has expired.
     *
     * <p>Loans are removed from the expiration queue in chronological order
     * until the next loan has not yet expired.
     */
    public void checkExpirationHeap(){

        while(true){
            Loan loan = repository.peekNextExpiration();
            if (loan == null || loan.getListingExpiresAt() > System.currentTimeMillis()) {
                break;
            }
            expire(repository.peekNextExpiration());
            repository.removeTopExpiration();
        }
    }

    /**
     * Processes all active loans whose borrowing period has ended.
     *
     * <p>Expired loans are automatically returned. The UUIDs of affected
     * borrowers are returned so callers can perform any additional cleanup.
     *
     * @return the borrowers whose loans were automatically returned
     */
    public Set<UUID> checkEndsAtHeap(){

        Set<UUID> removedBorrowers = new HashSet<>();

        while (true) {
            Loan loan = repository.peekNextEndAt();
            if (loan == null || loan.getActiveLoan().getEndsAt() > System.currentTimeMillis()) {
                break;
            }
            repository.removeTopEndAt();

            if (loan.getLoanState() != LoanState.BORROWED) {
                continue;
            }

            returnLoan(loan.getLoanUUID());
            removedBorrowers.add(loan.getActiveLoan().getBorrowerUUID());
        }

        return removedBorrowers;
    }

    /**
     * Applies the configured XP tax to a borrower's earned XP.
     *
     * <p>The taxed amount is credited to the lender and the loan is marked
     * dirty so the accrued rewards can be persisted later.
     *
     * @param loan the active loan
     * @param amount the XP earned by the borrower
     * @return the borrower's remaining XP after tax
     */
    public BigDecimal accruedXp(Loan loan, BigDecimal amount){
        int tax = loan.getLoanDeal().getXpTaxPercent();
        if(tax == 0){
            return amount;
        }
        BigDecimal taxedAmount = amount.multiply(BigDecimal.valueOf(tax, DECIMAL_MOVEMENT));
        loan.getActiveLoan().accruedXp(taxedAmount);
        dirtyLoans.add(loan.getLoanUUID());
        return amount.subtract(taxedAmount);
    }

    /**
     * Applies the configured energy tax to a borrower's earned energy.
     *
     * <p>The taxed amount is credited to the lender and the loan is marked
     * dirty so the accrued rewards can be persisted later.
     *
     * @param loan the active loan
     * @param amount the energy earned by the borrower
     * @return the borrower's remaining energy after tax
     */
    public BigDecimal accruedEnergy(Loan loan, BigDecimal amount){
        int tax = loan.getLoanDeal().getEnergyTaxPercent();
        if(tax == 0){
            return amount;
        }
        BigDecimal taxedAmount = amount.multiply(BigDecimal.valueOf(tax, DECIMAL_MOVEMENT));
        loan.getActiveLoan().accruedEnergy(taxedAmount);
        dirtyLoans.add(loan.getLoanUUID());
        return amount.subtract(taxedAmount);
    }

    /**
     * Persists all loans that have accumulated unflushed tax rewards.
     *
     * <p>Successfully persisted loans are removed from the dirty set.
     * Any storage failures are logged.
     */
    public void flushDirtyLoans(){

        try {
            for(UUID uuid : dirtyLoans){

                Loan loan = repository.findById(uuid).orElse(null);

                if(loan == null){
                    continue;
                }

                loanStorage.upsert(loan);
            }
            dirtyLoans.clear();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to flush dirty loans", e);
        }

    }

    /**
     * Persists all loans that have accumulated unflushed tax rewards.
     *
     * <p>Successfully persisted loans are removed from the dirty set.
     * Any storage failures are logged.
     */
    public void loadFromStorage(){
        for(Loan loan : loanStorage.loadAll()){
            repository.add(loan);
            if(loan.getLoanState() == LoanState.BORROWED){
                repository.recordBorrow(loan);
            }
        }
    }
}
