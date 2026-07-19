package io.github.ethan23.pickaxeLoans.model;

import io.github.ethan23.pickaxeLoans.item.LoanKeys;
import io.github.ethan23.pickaxeLoans.database.LoanRecord;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * A single pickaxe loan: the escrow pickaxe, the lender, the deal terms,
 * and the loan's position in its lifecycle.
 *
 * <p>The original pickaxe never leaves this object while the loan exists.
 * Borrowers receive a tagged copy from {@link #getLoanPickaxe()}, and the
 * lender gets the original back through {@link #getPickaxe()} when claiming.
 *
 * <p>State transitions are guarded: each mutator requires the loan to be in
 * the expected {@link LoanState} and throws {@link IllegalStateException}
 * otherwise, so an illegal transition can never corrupt a loan silently.
 *
 * <p>{@code activeLoan} is {@code null} if and only if the loan has never
 * been borrowed; it is set together with the transition to
 * {@link LoanState#BORROWED}.
 */
public class Loan {
    private final UUID loanUUID;
    private final ItemStack pickaxe;
    private final UUID lenderUUID;
    private final LoanDeal loanDeal;
    private final long createdAt;
    private final long listingExpiresAt;
    private LoanState loanState;
    private ActiveLoan activeLoan;

    /** How long a listing stays on the market before it expires. */
    private final static long EXPIRATION_TIME = 1000 * 60 * 60;

    private Loan(ActiveLoan activeLoan, LoanState loanState, long createdAt, long listingExpiresAt, LoanDeal loanDeal, UUID lenderUUID, ItemStack pickaxe, UUID loanUUID) {
        this.activeLoan = activeLoan;
        this.loanState = loanState;
        this.listingExpiresAt = listingExpiresAt;
        this.createdAt = createdAt;
        this.loanDeal = loanDeal;
        this.lenderUUID = lenderUUID;
        this.pickaxe = pickaxe;
        this.loanUUID = loanUUID;
    }

    /**
     * Creates a new {@link LoanState#LISTED} loan holding the given pickaxe
     * in escrow. The listing expires {@value #EXPIRATION_TIME} milliseconds
     * after creation.
     *
     * @param pickaxe the pickaxe to hold in escrow
     * @param lenderUUID the player listing the pickaxe
     * @param loanDeal the deal terms offered to borrowers
     */
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

    /**
     * Withdraws the listing at the lender's request.
     *
     * @throws IllegalStateException if the loan is not {@link LoanState#LISTED}
     */
    public void cancel() {
        requireState(LoanState.LISTED);
        this.loanState = LoanState.CANCELLED;
    }

    /**
     * Ends the listing because it timed out on the market.
     *
     * @throws IllegalStateException if the loan is not {@link LoanState#LISTED}
     */
    public void expire() {
        requireState(LoanState.LISTED);
        this.loanState = LoanState.EXPIRED;
    }

    /**
     * Moves the loan to {@link LoanState#BORROWED}, attaching the borrow
     * details in the same step so a borrowed loan always has an active loan.
     *
     * @param activeLoan the borrower and timing of this engagement
     * @throws IllegalStateException if the loan is not {@link LoanState#LISTED}
     */
    public void markBorrowed(ActiveLoan activeLoan) {
        requireState(LoanState.LISTED);
        this.activeLoan = activeLoan;         // set together with the transition
        this.loanState = LoanState.BORROWED;
    }

    /**
     * Ends the borrow. The accrued taxes stay on the active loan for the
     * lender to collect when claiming.
     *
     * @throws IllegalStateException if the loan is not {@link LoanState#BORROWED}
     */
    public void markReturned() {
        requireState(LoanState.BORROWED);
        this.loanState = LoanState.RETURNED;
    }

    /** Guards a transition, throwing if the loan is not in the expected state. */
    private void requireState(LoanState expected) {
        if (this.loanState != expected) {
            throw new IllegalStateException(
                    "Illegal transition: expected " + expected + " but loan was " + this.loanState);
        }
    }

    public UUID getLoanUUID() {
        return loanUUID;
    }

    /**
     * Returns a copy of the escrowed pickaxe, without any loan tag. This is
     * the item the lender receives back when claiming a finished loan.
     *
     * @return an untagged copy of the escrowed pickaxe
     */
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

    /**
     * Returns a copy of the pickaxe tagged with this loan's id in its
     * persistent data container. This is the item handed to the borrower;
     * the tag is what the drop/death/container listeners and inventory
     * cleanup scans key on to recognise and reclaim loaned items.
     *
     * @return a loan-tagged copy of the escrowed pickaxe
     */
    public ItemStack getLoanPickaxe() {
        ItemStack loanPickaxe = pickaxe.clone();
        ItemMeta pickaxeMeta = loanPickaxe.getItemMeta();
        pickaxeMeta.getPersistentDataContainer().set(LoanKeys.loanKey, PersistentDataType.STRING, this.loanUUID.toString());
        loanPickaxe.setItemMeta(pickaxeMeta);
        return loanPickaxe;
    }

    /**
     * Snapshots this loan into a {@link LoanRecord} for persistence. The
     * active-loan section is only present when the loan has been borrowed.
     *
     * @return an immutable snapshot of this loan's current state
     */
    public LoanRecord toRecord() {

        LoanRecord.ActiveLoanRecord activeLoanRecord = null;

        if (activeLoan != null) {
            activeLoanRecord = new LoanRecord.ActiveLoanRecord(this.activeLoan.getBorrowerUUID(), this.activeLoan.getXpAccrued(), this.activeLoan.getEnergyAccrued(),
                    this.activeLoan.getStartedAt(), this.activeLoan.getEndsAt());
        }

        return new LoanRecord(this.loanUUID, this.pickaxe.serializeAsBytes(), this.lenderUUID, this.loanDeal.getCostType(),
                this.loanDeal.getUpfrontCost(), this.loanDeal.getXpTaxPercent(), this.loanDeal.getEnergyTaxPercent(),
                this.loanDeal.getLoanDurationMillis(), this.createdAt, this.listingExpiresAt, this.loanState, activeLoanRecord);
    }

    /**
     * Rebuilds a loan from a stored {@link LoanRecord}, restoring its exact
     * state, timestamps, and any in-progress borrow.
     *
     * @param record the stored snapshot to rebuild from
     * @return the reconstructed loan
     */
    public static Loan fromRecord(LoanRecord record) {

        ActiveLoan activeLoan = record.activeLoanRecord() == null
                ? null
                : ActiveLoan.fromRecord(record.activeLoanRecord());

        return new Loan(activeLoan, record.loanState(), record.createdAt(), record.listingExpiresAt(), new LoanDeal(record.costType(), record.upFrontCost(), record.xpTaxPercent(), record.energyTaxPercent(), record.loanDurationMillis()), record.lenderUUID(), ItemStack.deserializeBytes(record.pickaxe()), record.loanUUID());
    }

}
