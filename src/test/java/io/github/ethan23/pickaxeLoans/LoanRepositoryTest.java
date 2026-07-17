package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.BorrowResult;
import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoanRepositoryTest {

    private Loan newLoan(UUID lenderUUID) {
        return new Loan(null, lenderUUID, CostType.MONEY, 100L, 10, 10, 60_000L);
    }

    @Test
    void createLoan_writesToAllThreeStructures() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        Loan loan = newLoan(lender);

        BorrowResult borrowResult = repo.createLoan(lender, loan);

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(loan, repo.findById(loan.getLoanUUID()).orElseThrow());
        assertTrue(repo.getLoansByLender(lender).contains(loan.getLoanUUID()));
        assertEquals(loan, repo.peekNextExpiration());

    }

    @Test
    void createLoan_secondLoanFromSameLender_bothTracked() {
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        BorrowResult borrowResult1 = repo.createLoan(lender, newLoan(lender));
        BorrowResult borrowResult2 = repo.createLoan(lender, newLoan(lender));

        assertEquals(BorrowResult.SUCCESS, borrowResult1);
        assertEquals(BorrowResult.SUCCESS, borrowResult2);
        assertEquals(2, repo.getLoansByLender(lender).size());
    }

    @Test
    void createLoan_duplicateLoanUUID(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        BorrowResult borrowResult1 = repo.createLoan(lender, loan);
        BorrowResult borrowResult2 = repo.createLoan(lender, loan);

        assertEquals(BorrowResult.SUCCESS, borrowResult1);
        assertEquals(BorrowResult.DUPLICATE_LOAN, borrowResult2);
        assertEquals(1, repo.getLoansByLender(lender).size());
        assertEquals(1, repo.getLoans().size());
    }

    @Test
    void cancelLoan_updatesLoanState(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.cancelLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertTrue(repo.findById(loan.getLoanUUID()).isPresent());
        assertEquals(LoanState.CANCELLED, repo.findById(loan.getLoanUUID()).get().getLoanState());
    }

    @Test
    void cancelLoan_randomLoanUUID(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.cancelLoan(lender);

        assertEquals(BorrowResult.NOT_FOUND, borrowResult);
    }

    @Test
    void cancelLoan_loanStateNotListed(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        loan.setLoanState(LoanState.RETURNED);

        BorrowResult borrowResult = repo.cancelLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.NOT_LISTED, borrowResult);
        assertTrue(repo.findById(loan.getLoanUUID()).isPresent());
        assertEquals(LoanState.RETURNED, loan.getLoanState());
    }

    @Test
    void cancelLoan_doubleCancel(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.cancelLoan(loan.getLoanUUID());
        BorrowResult borrowResult2 = repo.cancelLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(BorrowResult.NOT_LISTED, borrowResult2);
        assertEquals(LoanState.CANCELLED, loan.getLoanState());
    }

    @Test
    void borrowLoan_updateLoanState(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.borrowLoan(borrower, loan.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(LoanState.BORROWED,loan.getLoanState());
        assertNotNull(loan.getActiveLoan());
        assertEquals(loan, repo.peekNextEndAt());
        assertTrue(repo.getBorrowersLoanUUID(loan.getActiveLoan().getBorrowerUUID()).isPresent());
    }

    @Test
    void borrowLoan_playerDoubleBorrowAttempt(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(lender);
        Loan loan2 = newLoan(lender);

        repo.createLoan(lender, loan);
        repo.createLoan(lender, loan2);

        BorrowResult borrowResult = repo.borrowLoan(borrower, loan.getLoanUUID());
        BorrowResult borrowResult2 = repo.borrowLoan(borrower, loan2.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(BorrowResult.ALREADY_BORROWING, borrowResult2);
        assertEquals(LoanState.BORROWED, loan.getLoanState());
        assertEquals(LoanState.LISTED, loan2.getLoanState());
    }

    @Test
    void borrowLoan_sameLoanBorrowAttempt(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        UUID borrower2 = UUID.randomUUID();
        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.borrowLoan(borrower, loan.getLoanUUID());
        BorrowResult borrowResult2 = repo.borrowLoan(borrower2, loan.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(BorrowResult.NOT_LISTED, borrowResult2);
        assertEquals(LoanState.BORROWED, loan.getLoanState());
    }

    @Test
    void returnLoan_updateLoanState(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        repo.borrowLoan(borrower, loan.getLoanUUID());

        BorrowResult borrowResult = repo.returnLoan(loan.getLoanUUID());
        assertTrue(repo.getBorrowersLoanUUID(borrower).isEmpty());
        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(LoanState.RETURNED, loan.getLoanState());
    }

    @Test
    void returnLoan_incorrectLoanState(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();
        UUID borrower = UUID.randomUUID();
        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        repo.borrowLoan(borrower, loan.getLoanUUID());

        loan.setLoanState(LoanState.LISTED);

        BorrowResult borrowResult = repo.returnLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.NOT_BORROWED, borrowResult);
        assertEquals(LoanState.LISTED, loan.getLoanState());
    }


    @Test
    void expiredLoan_UpdateData(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        BorrowResult borrowResult = repo.expireLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.SUCCESS, borrowResult);
        assertEquals(LoanState.EXPIRED, loan.getLoanState());
    }

    @Test
    void expiredLoan_IncorrectLoanState(){
        LoanRepository repo = new LoanRepository();
        UUID lender = UUID.randomUUID();

        Loan loan = newLoan(lender);

        repo.createLoan(lender, loan);

        loan.setLoanState(LoanState.BORROWED);

        BorrowResult borrowResult = repo.expireLoan(loan.getLoanUUID());

        assertEquals(BorrowResult.NOT_LISTED, borrowResult);
        assertEquals(LoanState.BORROWED, loan.getLoanState());
    }

}
