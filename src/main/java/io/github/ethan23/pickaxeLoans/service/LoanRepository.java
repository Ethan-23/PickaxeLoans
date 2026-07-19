package io.github.ethan23.pickaxeLoans.service;

import io.github.ethan23.pickaxeLoans.model.Loan;

import java.util.*;

public class LoanRepository {

    //lenderUUID, Set<loanUUID>
    private final HashMap <UUID, LinkedHashSet<UUID>> lenderToLoans = new HashMap<>();

    //loanUUID, loan
    private final LinkedHashMap <UUID, Loan> loans = new LinkedHashMap<>();

    //borrowerUUID, loanUUID
    private final HashMap <UUID, UUID> borrowerToLoan = new HashMap<>();

    private final PriorityQueue<Loan> expirationHeap = new PriorityQueue<>(Comparator.comparingLong(Loan::getListingExpiresAt));
    private final PriorityQueue<Loan> endsAtHeap = new PriorityQueue<>(Comparator.comparingLong(loan -> loan.getActiveLoan().getEndsAt()));

    public boolean add(Loan loan){
        UUID loanUUID = loan.getLoanUUID();

        if(loans.containsKey(loanUUID)){
            return false;
        }

        loans.put(loanUUID, loan);
        lenderToLoans.computeIfAbsent(loan.getLenderUUID(), k -> new LinkedHashSet<>()).add(loanUUID);
        expirationHeap.add(loan);

        return true;
    }

    public void recordBorrow(Loan loan){
        borrowerToLoan.put(loan.getActiveLoan().getBorrowerUUID(), loan.getLoanUUID());
        endsAtHeap.add(loan);
    }

    public void recordReturn(Loan loan){
        borrowerToLoan.remove(loan.getActiveLoan().getBorrowerUUID());
    }

    public void deleteLoan(Loan loan) {
        loans.remove(loan.getLoanUUID());
        LinkedHashSet<UUID> bucket = lenderToLoans.get(loan.getLenderUUID());
        if(bucket != null) {
            bucket.remove(loan.getLoanUUID());
        }
    }

    public Optional<Loan> findById(UUID loanUUID) {
        return Optional.ofNullable(loans.get(loanUUID));
    }

    public boolean isBorrowing(UUID borrowerUUID) {
        return borrowerToLoan.containsKey(borrowerUUID);
    }

    public Optional<UUID> getBorrowersLoanUUID(UUID uuid){
        return Optional.ofNullable(borrowerToLoan.get(uuid));
    }

    public Set<UUID> getLoansByLender(UUID lenderUUID){
        if(!lenderToLoans.containsKey(lenderUUID)){
            return new HashSet<>(Set.of());
        }

        return Collections.unmodifiableSet(lenderToLoans.get(lenderUUID));
    }

    public Map<UUID, Loan> getLoans(){
        return Collections.unmodifiableMap(loans);
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

}
