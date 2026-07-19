package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import io.github.ethan23.pickaxeLoans.model.LoanResult;
import io.github.ethan23.pickaxeLoans.model.LoanState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoanServiceTest {

    private LoanService newService() {
        return new LoanService(new LoanRepository());
    }

    private Loan newLoan(UUID lenderUUID) {
        return new Loan(null, lenderUUID, new LoanDeal());
    }

    @Test
    void createListing_newLoan_isSuccess() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());

        assertEquals(LoanResult.SUCCESS, service.createListing(loan));
    }

    @Test
    void createListing_duplicate_isRejected() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());

        assertEquals(LoanResult.SUCCESS, service.createListing(loan));
        assertEquals(LoanResult.DUPLICATE_LOAN, service.createListing(loan));
    }

    @Test
    void cancel_listedLoan_isCancelled() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);

        assertEquals(LoanResult.SUCCESS, service.cancel(loan.getLoanUUID()));
        assertEquals(LoanState.CANCELLED, loan.getLoanState());
    }

    @Test
    void cancel_unknownLoan_isNotFound() {
        LoanService service = newService();

        assertEquals(LoanResult.NOT_FOUND, service.cancel(UUID.randomUUID()));
    }

    @Test
    void cancel_alreadyCancelled_isNotListed() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);
        service.cancel(loan.getLoanUUID());

        assertEquals(LoanResult.NOT_LISTED, service.cancel(loan.getLoanUUID()));
        assertEquals(LoanState.CANCELLED, loan.getLoanState());
    }

    @Test
    void borrow_listedLoan_isBorrowed() {
        LoanService service = newService();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);

        assertEquals(LoanResult.SUCCESS, service.borrow(borrower, loan.getLoanUUID()));
        assertEquals(LoanState.BORROWED, loan.getLoanState());
        assertEquals(loan, service.getBorrowersLoan(borrower));
    }

    @Test
    void borrow_whileAlreadyBorrowing_isRejected() {
        LoanService service = newService();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan1 = newLoan(lender);
        Loan loan2 = newLoan(lender);
        service.createListing(loan1);
        service.createListing(loan2);

        assertEquals(LoanResult.SUCCESS, service.borrow(borrower, loan1.getLoanUUID()));
        assertEquals(LoanResult.ALREADY_BORROWING, service.borrow(borrower, loan2.getLoanUUID()));
        assertEquals(LoanState.LISTED, loan2.getLoanState());
    }

    @Test
    void borrow_unknownLoan_isNotFound() {
        LoanService service = newService();

        assertEquals(LoanResult.NOT_FOUND, service.borrow(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void borrow_alreadyBorrowedLoan_isNotListed() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);
        service.borrow(UUID.randomUUID(), loan.getLoanUUID());

        assertEquals(LoanResult.NOT_LISTED, service.borrow(UUID.randomUUID(), loan.getLoanUUID()));
    }

    @Test
    void returnLoan_borrowedLoan_isReturned() {
        LoanService service = newService();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);
        service.borrow(borrower, loan.getLoanUUID());

        assertEquals(LoanResult.SUCCESS, service.returnLoan(loan.getLoanUUID()));
        assertEquals(LoanState.RETURNED, loan.getLoanState());
        assertNull(service.getBorrowersLoan(borrower));
    }

    @Test
    void returnLoan_unknownLoan_isNotFound() {
        LoanService service = newService();

        assertEquals(LoanResult.NOT_FOUND, service.returnLoan(UUID.randomUUID()));
    }

    @Test
    void returnLoan_notBorrowed_isRejected() {
        LoanService service = newService();
        Loan loan = newLoan(UUID.randomUUID());
        service.createListing(loan);

        assertEquals(LoanResult.NOT_BORROWED, service.returnLoan(loan.getLoanUUID()));
        assertEquals(LoanState.LISTED, loan.getLoanState());
    }

    @Test
    void getListedLoans_excludesBorrowed() {
        LoanService service = newService();
        UUID lender = UUID.randomUUID();
        Loan listed = newLoan(lender);
        Loan borrowed = newLoan(lender);
        service.createListing(listed);
        service.createListing(borrowed);
        service.borrow(UUID.randomUUID(), borrowed.getLoanUUID());

        List<Loan> result = service.getListedLoans();

        assertTrue(result.contains(listed));
        assertFalse(result.contains(borrowed));
    }

    @Test
    void getLenderLoans_returnsOnlyThatLender() {
        LoanService service = newService();
        UUID lender = UUID.randomUUID();
        Loan a = newLoan(lender);
        Loan b = newLoan(lender);
        Loan other = newLoan(UUID.randomUUID());
        service.createListing(a);
        service.createListing(b);
        service.createListing(other);

        List<Loan> result = service.getLenderLoans(lender);

        assertEquals(2, result.size());
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
        assertFalse(result.contains(other));
    }

    @Test
    void getBorrowersLoan_noActiveBorrow_isNull() {
        LoanService service = newService();

        assertNull(service.getBorrowersLoan(UUID.randomUUID()));
    }
}
