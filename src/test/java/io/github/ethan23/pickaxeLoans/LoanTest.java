package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.ActiveLoan;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import io.github.ethan23.pickaxeLoans.model.LoanState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoanTest {

    private static final long LISTING_EXPIRATION_MILLIS = 60 * 60 * 1000;

    private Loan newLoan() {
        return new Loan(null, UUID.randomUUID(), new LoanDeal());
    }

    private ActiveLoan newActiveLoan() {
        return new ActiveLoan(UUID.randomUUID(), 60_000L);
    }

    @Test
    void newLoan_startsListedWithNoActiveLoan() {
        Loan loan = newLoan();

        assertEquals(LoanState.LISTED, loan.getLoanState());
        assertNull(loan.getActiveLoan());
        assertNotNull(loan.getLoanUUID());
    }

    @Test
    void newLoan_listingExpiresOneHourAfterCreation() {
        Loan loan = newLoan();

        assertEquals(loan.getCreatedAt() + LISTING_EXPIRATION_MILLIS, loan.getListingExpiresAt());
    }

    @Test
    void cancel_fromListed_isCancelled() {
        Loan loan = newLoan();

        loan.cancel();

        assertEquals(LoanState.CANCELLED, loan.getLoanState());
    }

    @Test
    void cancel_whenAlreadyCancelled_throws() {
        Loan loan = newLoan();
        loan.cancel();

        assertThrows(IllegalStateException.class, loan::cancel);
    }

    @Test
    void expire_fromListed_isExpired() {
        Loan loan = newLoan();

        loan.expire();

        assertEquals(LoanState.EXPIRED, loan.getLoanState());
    }

    @Test
    void expire_whenCancelled_throws() {
        Loan loan = newLoan();
        loan.cancel();

        assertThrows(IllegalStateException.class, loan::expire);
    }

    @Test
    void markBorrowed_fromListed_setsStateAndActiveLoanTogether() {
        Loan loan = newLoan();
        ActiveLoan activeLoan = newActiveLoan();

        loan.markBorrowed(activeLoan);

        assertEquals(LoanState.BORROWED, loan.getLoanState());
        assertEquals(activeLoan, loan.getActiveLoan());
    }

    @Test
    void markBorrowed_whenAlreadyBorrowed_throws() {
        Loan loan = newLoan();
        loan.markBorrowed(newActiveLoan());

        assertThrows(IllegalStateException.class, () -> loan.markBorrowed(newActiveLoan()));
    }

    @Test
    void markReturned_fromBorrowed_isReturned() {
        Loan loan = newLoan();
        loan.markBorrowed(newActiveLoan());

        loan.markReturned();

        assertEquals(LoanState.RETURNED, loan.getLoanState());
    }

    @Test
    void markReturned_whenOnlyListed_throws() {
        Loan loan = newLoan();

        assertThrows(IllegalStateException.class, loan::markReturned);
    }

    @Test
    void cancel_whenBorrowed_throws() {
        Loan loan = newLoan();
        loan.markBorrowed(newActiveLoan());

        assertThrows(IllegalStateException.class, loan::cancel);
    }

    @Test
    void markBorrowed_whenReturned_throws() {
        Loan loan = newLoan();
        loan.markBorrowed(newActiveLoan());
        loan.markReturned();

        assertThrows(IllegalStateException.class, () -> loan.markBorrowed(newActiveLoan()));
    }
}
