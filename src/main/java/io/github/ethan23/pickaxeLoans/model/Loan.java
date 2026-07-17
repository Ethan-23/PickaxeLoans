package io.github.ethan23.pickaxeLoans.model;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Loan {
    private final UUID loanUUID;
    private final ItemStack pickaxe;
    private final UUID lenderUUID;
    private CostType costType;
    private long upfrontCost;
    private int xpTaxPercent;
    private int energyTaxPercent;
    private final long createdAt;
    private final long listingExpiresAt;
    private long loanDurationMillis;
    private LoanState loanState;
    private ActiveLoan activeLoan;

    private final static long EXPIRATION_TIME = 3_600_000;

    public Loan(ItemStack pickaxe, UUID lenderUUID, CostType costType, long upfrontCost, int xpTaxPercent, int energyTaxPercent, long loanDurationMillis) {
        this.loanUUID = UUID.randomUUID();
        this.pickaxe = pickaxe;
        this.lenderUUID = lenderUUID;
        this.costType = costType;
        this.upfrontCost = upfrontCost;
        this.xpTaxPercent = xpTaxPercent;
        this.energyTaxPercent = energyTaxPercent;
        this.createdAt = System.currentTimeMillis();
        this.loanDurationMillis = loanDurationMillis;
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

    public CostType getCostType() {
        return costType;
    }

    public long getUpfrontCost() {
        return upfrontCost;
    }

    public int getXpTaxPercent() {
        return xpTaxPercent;
    }

    public int getEnergyTaxPercent() {
        return energyTaxPercent;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getListingExpiresAt() {
        return listingExpiresAt;
    }

    public long getLoanDurationMillis() {
        return loanDurationMillis;
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
