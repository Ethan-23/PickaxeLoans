package io.github.ethan23.pickaxeLoans.model;

/**
 * Lifecycle states of a {@link Loan}.
 *
 * <p>Valid transitions:
 * <pre>
 * LISTED ---&gt; BORROWED ---&gt; RETURNED
 *    |-------&gt; EXPIRED
 *    '-------&gt; CANCELLED
 * </pre>
 *
 * <p>{@code RETURNED}, {@code EXPIRED}, and {@code CANCELLED} are terminal:
 * the loan can never be re-listed, and it remains claimable by the lender
 * until it is deleted. Transitions are enforced by {@link Loan}, which
 * throws on any move not shown above.
 */
public enum LoanState {
    /** Listed on the loan market and available to borrow. */
    LISTED,
    /** Held by a borrower; taxes accrue while in this state. */
    BORROWED,
    /** The listing timed out before anyone borrowed it. */
    EXPIRED,
    /** The borrow ended (timer, death, or manual return); awaits lender claim. */
    RETURNED,
    /** The lender withdrew the listing before it was borrowed. */
    CANCELLED
}
