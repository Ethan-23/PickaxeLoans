package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.LoanState;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable snapshot of a {@link io.github.ethan23.pickaxeLoans.model.Loan}
 * used to move loan data across the persistence boundary.
 *
 * <p>{@code activeLoanRecord} is {@code null} if and only if the loan has
 * never been borrowed — storage implementations rely on that rule to decide
 * whether the borrower column group is present.
 *
 * <p>The pickaxe is carried as serialized bytes
 * ({@code ItemStack.serializeAsBytes}) so the record holds no live Bukkit
 * state.
 */
public record LoanRecord(UUID loanUUID, byte[] pickaxe, UUID lenderUUID, CostType costType, double upFrontCost,
                         int xpTaxPercent, int energyTaxPercent, long loanDurationMillis, long createdAt,
                         long listingExpiresAt, LoanState loanState, ActiveLoanRecord activeLoanRecord) {

    /** Snapshot of an in-progress borrow: who holds the pickaxe, the accrued taxes, and the timing. */
    public record ActiveLoanRecord(UUID borrowerUUID, BigDecimal xpAccrued,
                                   BigDecimal energyAccrued, long startedAt, long endsAt) { }
}
