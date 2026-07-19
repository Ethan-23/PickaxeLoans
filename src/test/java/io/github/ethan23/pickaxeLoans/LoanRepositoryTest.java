package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.ActiveLoan;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoanRepositoryTest {

    private Loan newLoan(UUID lenderUUID) {
        return new Loan(null, lenderUUID, new LoanDeal());
    }

    private Loan borrowedLoan(UUID lenderUUID, UUID borrowerUUID, long durationMillis) {
        Loan loan = newLoan(lenderUUID);
        loan.markBorrowed(new ActiveLoan(borrowerUUID, durationMillis));
        return loan;
    }

    @Test
    void add_storesLoanInAllLookups() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        Loan loan = newLoan(lender);

        assertTrue(repo.add(loan));
        assertEquals(loan, repo.findById(loan.getLoanUUID()).orElseThrow());
        assertTrue(repo.getLoansByLender(lender).contains(loan.getLoanUUID()));
        assertEquals(loan, repo.peekNextExpiration());
    }

    @Test
    void add_duplicateUUID_isRejected() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        Loan loan = newLoan(lender);

        assertTrue(repo.add(loan));
        assertFalse(repo.add(loan));
        assertEquals(1, repo.getLoans().size());
        assertEquals(1, repo.getLoansByLender(lender).size());
    }

    @Test
    void add_multipleLoansSameLender_allTracked() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        repo.add(newLoan(lender));
        repo.add(newLoan(lender));

        assertEquals(2, repo.getLoansByLender(lender).size());
    }

    @Test
    void findById_unknownUUID_isEmpty() {
        LoanRepository repo = new LoanRepository();

        assertTrue(repo.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void getLoansByLender_unknownLender_isEmpty() {
        LoanRepository repo = new LoanRepository();

        assertTrue(repo.getLoansByLender(UUID.randomUUID()).isEmpty());
    }

    @Test
    void recordBorrow_tracksBorrower() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = borrowedLoan(lender, borrower, 60_000L);

        repo.add(loan);
        repo.recordBorrow(loan);

        assertTrue(repo.isBorrowing(borrower));
        assertEquals(loan.getLoanUUID(), repo.getBorrowersLoanUUID(borrower).orElseThrow());
    }

    @Test
    void recordReturn_clearsBorrower() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = borrowedLoan(lender, borrower, 60_000L);

        repo.add(loan);
        repo.recordBorrow(loan);
        repo.recordReturn(loan);

        assertFalse(repo.isBorrowing(borrower));
        assertTrue(repo.getBorrowersLoanUUID(borrower).isEmpty());
    }

    @Test
    void deleteLoan_removesFromLoansAndLender() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        Loan loan = newLoan(lender);

        repo.add(loan);
        repo.deleteLoan(loan);

        assertTrue(repo.findById(loan.getLoanUUID()).isEmpty());
        assertFalse(repo.getLoansByLender(lender).contains(loan.getLoanUUID()));
    }

    @Test
    void endsAtHeap_ordersBySoonestEnd() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        Loan longLoan = borrowedLoan(lender, UUID.randomUUID(), 600_000L);
        Loan shortLoan = borrowedLoan(lender, UUID.randomUUID(), 10_000L);

        repo.add(longLoan);
        repo.add(shortLoan);
        repo.recordBorrow(longLoan);
        repo.recordBorrow(shortLoan);

        assertEquals(shortLoan, repo.peekNextEndAt());
        repo.removeTopEndAt();
        assertEquals(longLoan, repo.peekNextEndAt());
    }

    @Test
    void removeTopExpiration_removesHead() {
        LoanRepository repo = new LoanRepository();
        Loan loan = newLoan(UUID.randomUUID());

        repo.add(loan);
        assertEquals(loan, repo.peekNextExpiration());

        repo.removeTopExpiration();
        assertNull(repo.peekNextExpiration());
    }

    @Test
    void getLoans_isUnmodifiable() {
        LoanRepository repo = new LoanRepository();
        Loan loan = newLoan(UUID.randomUUID());
        repo.add(loan);

        Map<UUID, Loan> view = repo.getLoans();
        assertThrows(UnsupportedOperationException.class, () -> view.put(UUID.randomUUID(), loan));
    }

    @Test
    void getLoansByLender_isUnmodifiable() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        repo.add(newLoan(lender));

        Set<UUID> view = repo.getLoansByLender(lender);
        assertThrows(UnsupportedOperationException.class, () -> view.add(UUID.randomUUID()));
    }
}
