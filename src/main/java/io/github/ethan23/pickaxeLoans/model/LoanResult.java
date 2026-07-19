package io.github.ethan23.pickaxeLoans.model;

/**
 * Outcome of a {@link io.github.ethan23.pickaxeLoans.service.LoanService}
 * operation.
 *
 * <p>Returned instead of throwing so that callers (usually menus) can
 * translate each case into player feedback without try/catch plumbing.
 */
public enum LoanResult {
    /** The operation completed. */
    SUCCESS,
    /** No loan exists with the given id. */
    NOT_FOUND,
    /** The loan is not in the {@link LoanState#LISTED} state. */
    NOT_LISTED,
    /** The loan is not in the {@link LoanState#BORROWED} state. */
    NOT_BORROWED,
    /** The borrower already holds an active loan. */
    ALREADY_BORROWING,
    /** The borrower is the loan's own lender. */
    LENDERS_LOAN,
    /** A loan with this id is already registered. */
    DUPLICATE_LOAN,
    /** The lender has reached the maximum number of listings. */
    MAX_LOANS
}
