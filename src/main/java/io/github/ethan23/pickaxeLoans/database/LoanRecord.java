package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.LoanState;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanRecord(UUID loanUUID, byte[] pickaxe, UUID lenderUUID, CostType costType, double upFrontCost,
                         int xpTaxPercent, int energyTaxPercent, long loanDurationMillis, long createdAt,
                         long listingExpiresAt, LoanState loanState, ActiveLoanRecord activeLoanRecord) {

    public record ActiveLoanRecord(UUID borrowerUUID, BigDecimal xpAccrued,
                                   BigDecimal energyAccrued, long startedAt, long endsAt) { }
}
