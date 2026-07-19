package io.github.ethan23.pickaxeLoans.service;

import io.github.ethan23.pickaxeLoans.database.LoanStorage;
import io.github.ethan23.pickaxeLoans.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoanService {

    private final LoanRepository repository;
    private final LoanStorage loanStorage;
    private final Set<UUID> dirtyLoans;
    private final Logger logger;

    private static final int DECIMAL_MOVEMENT = 2;

    public LoanService(LoanRepository repository, LoanStorage loanStorage, Logger logger) {
        this.repository = repository;
        this.loanStorage = loanStorage;
        this.logger = logger;
        this.dirtyLoans = new HashSet<>();
    }

    public LoanResult createListing(Loan loan) {
        if (!repository.add(loan)) {
            return LoanResult.DUPLICATE_LOAN;
        }
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

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

    public LoanResult borrow(UUID borrowerUUID, UUID loanUUID) {
        if (repository.isBorrowing(borrowerUUID)){
            return LoanResult.ALREADY_BORROWING;
        }

        Loan loan = repository.findById(loanUUID).orElse(null);
        if (loan == null){
            return LoanResult.NOT_FOUND;
        }
        if (loan.getLoanState() != LoanState.LISTED){
            return LoanResult.NOT_LISTED;
        }

        loan.markBorrowed(new ActiveLoan(borrowerUUID, loan.getLoanDeal().getLoanDurationMillis()));
        repository.recordBorrow(loan);
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    public LoanResult exprire(Loan loan){

        if(loan.getLoanState() != LoanState.LISTED){
            return LoanResult.NOT_LISTED;
        }
        loan.expire();
        loanStorage.upsert(loan);
        return LoanResult.SUCCESS;
    }

    public void deleteLoan(Loan loan){
        repository.deleteLoan(loan);
        loanStorage.delete(loan.getLoanUUID());
    }

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

    public void checkExpirationHeap(){

        while(true){
            Loan loan = repository.peekNextExpiration();
            if (loan == null || loan.getListingExpiresAt() > System.currentTimeMillis()) {
                break;
            }
            exprire(repository.peekNextExpiration());
            repository.removeTopExpiration();
        }
    }

    public Set<UUID> checkEndsAtHeap(){

        Set<UUID> removedBorrowers = new HashSet<>();

        while (true) {
            Loan loan = repository.peekNextEndAt();
            if (loan == null || loan.getActiveLoan().getEndsAt() > System.currentTimeMillis()) {
                break;
            }
            returnLoan(loan.getLoanUUID());
            repository.removeTopEndAt();
            removedBorrowers.add(loan.getActiveLoan().getBorrowerUUID());
        }

        return removedBorrowers;
    }

    public void accruedXp(Loan loan, BigDecimal amount){
        int tax = loan.getLoanDeal().getXpTaxPercent();
        if(tax == 0){
            return;
        }
        loan.getActiveLoan().accruedXp(amount.multiply(BigDecimal.valueOf(tax, DECIMAL_MOVEMENT)));
        dirtyLoans.add(loan.getLoanUUID());
    }

    public void accruedEnergy(Loan loan, BigDecimal amount){
        int tax = loan.getLoanDeal().getEnergyTaxPercent();
        if(tax == 0){
            return;
        }
        loan.getActiveLoan().accruedEnergy(amount.multiply(BigDecimal.valueOf(tax, DECIMAL_MOVEMENT)));
        dirtyLoans.add(loan.getLoanUUID());
    }

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

    public void loadFromStorage(){
        for(Loan loan : loanStorage.loadAll()){
            repository.add(loan);
            if(loan.getLoanState() == LoanState.BORROWED){
                repository.recordBorrow(loan);
            }
        }
    }

}
