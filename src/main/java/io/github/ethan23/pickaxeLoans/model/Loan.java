package io.github.ethan23.pickaxeLoans.model;

import io.github.ethan23.pickaxeLoans.LoanKeys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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

    public Loan(ItemStack pickaxe, UUID lenderUUID, LoanDeal loanDeal) {
        this.loanUUID = UUID.randomUUID();
        this.pickaxe = pickaxe;
        this.lenderUUID = lenderUUID;
        this.loanDeal = loanDeal;
        this.createdAt = System.currentTimeMillis();
        this.listingExpiresAt = this.createdAt + EXPIRATION_TIME;
        this.loanState = LoanState.LISTED;
        this.activeLoan = null;
    }

    public void cancel() {
        requireState(LoanState.LISTED);
        this.loanState = LoanState.CANCELLED;
    }

    public void expire() {
        requireState(LoanState.LISTED);
        this.loanState = LoanState.EXPIRED;
    }

    public void markBorrowed(ActiveLoan activeLoan) {
        requireState(LoanState.LISTED);
        this.activeLoan = activeLoan;         // set together with the transition
        this.loanState = LoanState.BORROWED;
    }

    public void markReturned() {
        requireState(LoanState.BORROWED);
        this.loanState = LoanState.RETURNED;
    }

    private void requireState(LoanState expected) {
        if (this.loanState != expected) {
            throw new IllegalStateException(
                    "Illegal transition: expected " + expected + " but loan was " + this.loanState);
        }
    }

    public UUID getLoanUUID() {
        return loanUUID;
    }

    public ItemStack getPickaxe() {
        return pickaxe.clone();
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

    public ItemStack getLoanPickaxe(){
        ItemStack loanPickaxe = pickaxe.clone();
        ItemMeta pickaxeMeta = loanPickaxe.getItemMeta();
        pickaxeMeta.getPersistentDataContainer().set(LoanKeys.loanKey, PersistentDataType.STRING, this.loanUUID.toString());
        loanPickaxe.setItemMeta(pickaxeMeta);
        return loanPickaxe;
    }

}
