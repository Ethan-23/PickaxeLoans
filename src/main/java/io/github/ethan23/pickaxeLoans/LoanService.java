package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.ActiveLoan;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanResult;
import io.github.ethan23.pickaxeLoans.model.LoanState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoanService {

    private final LoanRepository repository;

    public LoanService(LoanRepository repository) {
        this.repository = repository;
    }

    public LoanResult createListing(Loan loan) {
        if (!repository.add(loan)) {
            return LoanResult.DUPLICATE_LOAN;
        }
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
        return LoanResult.SUCCESS;
    }

    public void deleteLoan(Loan loan){
        repository.deleteLoan(loan);
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
}
