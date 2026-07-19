package io.github.ethan23.pickaxeLoans.model;

import io.github.ethan23.pickaxeLoans.database.LoanRecord;

import java.math.BigDecimal;
import java.util.UUID;

public class ActiveLoan {

    private final UUID borrowerUUID;
    private BigDecimal xpAccrued;
    private BigDecimal energyAccrued;
    private final long startedAt;
    private final long endsAt;

    private ActiveLoan(UUID borrowerUUID, BigDecimal xpAccrued, BigDecimal energyAccrued, long startedAt, long endsAt) {
        this.borrowerUUID = borrowerUUID;
        this.xpAccrued = xpAccrued;
        this.energyAccrued = energyAccrued;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }

    public ActiveLoan(UUID borrowerUUID, long loanDurationMillis) {
        this.borrowerUUID = borrowerUUID;
        this.xpAccrued = BigDecimal.ZERO;
        this.energyAccrued = BigDecimal.ZERO;
        this.startedAt = System.currentTimeMillis();
        this.endsAt = this.startedAt + loanDurationMillis;
    }

    public void accruedXp(BigDecimal amount){
        this.xpAccrued = this.xpAccrued.add(amount);
    }

    public void accruedEnergy(BigDecimal amount){
        this.energyAccrued = this.energyAccrued.add(amount);
    }

    public UUID getBorrowerUUID() {
        return borrowerUUID;
    }

    public BigDecimal getXpAccrued() {
        return xpAccrued;
    }

    public BigDecimal getEnergyAccrued() {
        return energyAccrued;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndsAt() {
        return endsAt;
    }

    public static ActiveLoan fromRecord(LoanRecord.ActiveLoanRecord activeLoanRecord){
        return new ActiveLoan(activeLoanRecord.borrowerUUID(), activeLoanRecord.xpAccrued(), activeLoanRecord.energyAccrued(), activeLoanRecord.startedAt(), activeLoanRecord.endsAt());
    }
}
