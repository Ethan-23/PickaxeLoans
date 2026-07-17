package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.ActiveLoan;
import io.github.ethan23.pickaxeLoans.model.BorrowResult;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanState;

import java.util.*;

public class LoanRepository {

    //lenderUUID, Set<loanUUID>
    private final HashMap <UUID, Set<UUID>> lenderToLoans = new HashMap<>();

    //loanUUID, loan
    private final LinkedHashMap <UUID, Loan> loans = new LinkedHashMap<>();

    //borrowerUUID, loanUUID
    private final HashMap <UUID, UUID> borrowerToLoan = new HashMap<>();

    private final PriorityQueue<Loan> expirationHeap = new PriorityQueue<>(Comparator.comparingLong(Loan::getListingExpiresAt));
    private final PriorityQueue<Loan> endsAtHeap = new PriorityQueue<>(Comparator.comparingLong(loan -> loan.getActiveLoan().getEndsAt()));

    public BorrowResult createLoan(UUID lender, Loan loan){
        if(!lenderToLoans.containsKey(lender)){
            lenderToLoans.put(lender, new HashSet<>());
        }

        UUID loanUUID = loan.getLoanUUID();

        if(lenderToLoans.get(lender).contains(loanUUID)){
            return BorrowResult.DUPLICATE_LOAN;
        }

        lenderToLoans.get(lender).add(loan.getLoanUUID());
        loans.put(loan.getLoanUUID(), loan);
        expirationHeap.add(loan);

        loan.setLoanState(LoanState.LISTED);

        return BorrowResult.SUCCESS;
    }

    public BorrowResult cancelLoan(UUID loanUUID){
        if(!loans.containsKey(loanUUID)){
            return BorrowResult.NOT_FOUND;
        }

        Loan loan = loans.get(loanUUID);

        if(loan.getLoanState() != LoanState.LISTED){
            return BorrowResult.NOT_LISTED;
        }

        loan.setLoanState(LoanState.CANCELLED);

        return BorrowResult.SUCCESS;
    }

    public BorrowResult borrowLoan(UUID borrowerUUID, UUID loanUUID){

        if(borrowerToLoan.containsKey(borrowerUUID)){
            return BorrowResult.ALREADY_BORROWING;
        }

        if(!loans.containsKey(loanUUID)){
            return BorrowResult.NOT_FOUND;
        }

        Loan loan = loans.get(loanUUID);

        if(loan.getLoanState() != LoanState.LISTED){
            return BorrowResult.NOT_LISTED;
        }

        ActiveLoan activeLoan = new ActiveLoan(
                borrowerUUID,
                loan.getLoanDeal().getLoanDurationMillis()
        );
        loan.setActiveLoan(activeLoan);

        borrowerToLoan.put(borrowerUUID, loanUUID);
        endsAtHeap.add(loan);

        loan.setLoanState(LoanState.BORROWED);

        return BorrowResult.SUCCESS;
    }

    public BorrowResult returnLoan(UUID loanUUID){
        if(!loans.containsKey(loanUUID)){
            return BorrowResult.NOT_FOUND;
        }

        Loan loan = loans.get(loanUUID);

        if(loan.getLoanState() != LoanState.BORROWED){
            return BorrowResult.NOT_BORROWED;
        }

        borrowerToLoan.remove(loan.getActiveLoan().getBorrowerUUID());

        loan.setLoanState(LoanState.RETURNED);

        return BorrowResult.SUCCESS;
    }

    public BorrowResult expireLoan(UUID loanUUID){
        if(!loans.containsKey(loanUUID)){
            return BorrowResult.NOT_FOUND;
        }

        Loan loan = loans.get(loanUUID);

        if(loan.getLoanState() != LoanState.LISTED){
            return BorrowResult.NOT_LISTED;
        }

        loan.setLoanState(LoanState.EXPIRED);
        return BorrowResult.SUCCESS;
    }

    public Optional<Loan> findById(UUID loanUUID){
        return Optional.ofNullable(loans.get(loanUUID));
    }

    public Set<UUID> getLoansByLender(UUID lenderUUID){
        if(!lenderToLoans.containsKey(lenderUUID)){
            return new HashSet<>(Set.of());
        }

        return Collections.unmodifiableSet(lenderToLoans.get(lenderUUID));
    }

    public Loan peekNextExpiration(){
        return expirationHeap.peek();
    }

    public Loan peekNextEndAt(){
        return endsAtHeap.peek();
    }

    public void removeTopEndAt(){
        endsAtHeap.poll();
    }

    public void removeTopExpiration() {
        expirationHeap.poll();
    }

    public Map<UUID, Loan> getLoans(){
        return Collections.unmodifiableMap(loans);
    }

    public Optional<UUID> getBorrowersLoanUUID(UUID uuid){
        return Optional.ofNullable(borrowerToLoan.get(uuid));
    }

}
