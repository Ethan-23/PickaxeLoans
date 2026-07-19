package io.github.ethan23.pickaxeLoans.model;

import java.math.BigDecimal;
import java.util.UUID;

public class ActiveLoan {

    UUID borrowerUUID;
    BigDecimal xpTax;
    BigDecimal energyTax;
    long startedAt;
    long endsAt;

    public ActiveLoan(UUID borrowerUUID, long loanDurationMillis) {
        this.borrowerUUID = borrowerUUID;
        this.xpTax = BigDecimal.valueOf(0);
        this.energyTax = BigDecimal.valueOf(0);
        this.startedAt = System.currentTimeMillis();
        this.endsAt = this.startedAt + loanDurationMillis;
    }

    public UUID getBorrowerUUID() {
        return borrowerUUID;
    }

    public BigDecimal getXpTax() {
        return xpTax;
    }

    public BigDecimal getEnergyTax() {
        return energyTax;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndsAt() {
        return endsAt;
    }
}
