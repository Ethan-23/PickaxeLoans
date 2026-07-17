package io.github.ethan23.pickaxeLoans.model;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Loan {
    private final UUID loanUUID;
    private final ItemStack pickaxe;
    private final UUID lenderUUID;
    private final LoanDeal loanDeal;
    private final long createdAt;
    private final long listingExpiresAt;
    private LoanState loanState;
    private ActiveLoan activeLoan;

    private final static long EXPIRATION_TIME = 3_600_000;

    public Loan(ItemStack pickaxe, UUID lenderUUID) {
        this.loanUUID = UUID.randomUUID();
        this.pickaxe = pickaxe;
        this.lenderUUID = lenderUUID;
        this.loanDeal = new LoanDeal();
        this.createdAt = System.currentTimeMillis();
        this.listingExpiresAt = this.createdAt + EXPIRATION_TIME;
        this.loanState = LoanState.LISTED;
        this.activeLoan = null;
    }

    public UUID getLoanUUID() {
        return loanUUID;
    }

    public ItemStack getPickaxe() {
        return pickaxe;
    }

    public UUID getLenderUUID() {
        return lenderUUID;
    }

    public LoanDeal getLoanDeal() {
        return loanDeal;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getListingExpiresAt() {
        return listingExpiresAt;
    }

    public LoanState getLoanState() {
        return loanState;
    }

    public ActiveLoan getActiveLoan() {
        return activeLoan;
    }

    public void setActiveLoan(ActiveLoan activeLoan) {
        this.activeLoan = activeLoan;
    }

    public void setLoanState(LoanState loanState) {
        this.loanState = loanState;
    }
}
