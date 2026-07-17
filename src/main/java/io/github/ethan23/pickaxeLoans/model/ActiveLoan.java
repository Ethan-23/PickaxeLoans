package io.github.ethan23.pickaxeLoans.model;

import java.util.UUID;

public class ActiveLoan {

    UUID borrowerUUID;
    float xpTax;
    float energyTax;
    long startedAt;
    long endsAt;

    public ActiveLoan(UUID borrowerUUID, long loanDurationMillis) {
        this.borrowerUUID = borrowerUUID;
        this.xpTax = 0;
        this.energyTax = 0;
        this.startedAt = System.currentTimeMillis();
        this.endsAt = this.startedAt + loanDurationMillis;
    }

    public UUID getBorrowerUUID() {
        return borrowerUUID;
    }

    public float getXpTax() {
        return xpTax;
    }

    public float getEnergyTax() {
        return energyTax;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndsAt() {
        return endsAt;
    }
}
