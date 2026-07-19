package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import io.github.ethan23.pickaxeLoans.service.LoanRepository;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class LoanServiceTaxTest {

    private InMemoryLoanStorage storage;
    private LoanService service;

    private LoanService newService() {
        storage = new InMemoryLoanStorage();
        service = new LoanService(new LoanRepository(), storage, Logger.getGlobal());
        return service;
    }

    private Loan borrowedLoan(int xpTaxPercent, int energyTaxPercent) {
        LoanDeal deal = new LoanDeal();
        deal.setXpTaxPercent(xpTaxPercent);
        deal.setEnergyTaxPercent(energyTaxPercent);
        Loan loan = new Loan(null, UUID.randomUUID(), deal);
        service.createListing(loan);
        service.borrow(UUID.randomUUID(), loan.getLoanUUID());
        return loan;
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "expected " + expected + " but was " + actual);
    }

    @Test
    void accruedXp_zeroTax_returnsFullAmountAndAccruesNothing() {
        newService();
        Loan loan = borrowedLoan(0, 0);

        BigDecimal kept = service.accruedXp(loan, new BigDecimal("100"));

        assertAmount("100", kept);
        assertAmount("0", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedXp_withTax_splitsAmountBetweenBorrowerAndLender() {
        newService();
        Loan loan = borrowedLoan(25, 0);

        BigDecimal kept = service.accruedXp(loan, new BigDecimal("100"));

        assertAmount("75", kept);
        assertAmount("25", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedXp_fractionalResult_isExact() {
        newService();
        Loan loan = borrowedLoan(33, 0);

        BigDecimal kept = service.accruedXp(loan, new BigDecimal("10"));

        assertAmount("6.70", kept);
        assertAmount("3.30", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedXp_fullTax_borrowerKeepsNothing() {
        newService();
        Loan loan = borrowedLoan(100, 0);

        BigDecimal kept = service.accruedXp(loan, new BigDecimal("50"));

        assertAmount("0", kept);
        assertAmount("50", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedXp_multipleAccruals_sumUp() {
        newService();
        Loan loan = borrowedLoan(10, 0);

        service.accruedXp(loan, new BigDecimal("100"));
        service.accruedXp(loan, new BigDecimal("50"));

        assertAmount("15", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedEnergy_zeroTax_returnsFullAmountAndAccruesNothing() {
        newService();
        Loan loan = borrowedLoan(0, 0);

        BigDecimal kept = service.accruedEnergy(loan, new BigDecimal("200"));

        assertAmount("200", kept);
        assertAmount("0", loan.getActiveLoan().getEnergyAccrued());
    }

    @Test
    void accruedEnergy_withTax_splitsAmountBetweenBorrowerAndLender() {
        newService();
        Loan loan = borrowedLoan(0, 50);

        BigDecimal kept = service.accruedEnergy(loan, new BigDecimal("80"));

        assertAmount("40", kept);
        assertAmount("40", loan.getActiveLoan().getEnergyAccrued());
    }

    @Test
    void accruedEnergy_usesEnergyTaxNotXpTax() {
        newService();
        Loan loan = borrowedLoan(25, 10);

        BigDecimal kept = service.accruedEnergy(loan, new BigDecimal("100"));

        assertAmount("90", kept);
        assertAmount("10", loan.getActiveLoan().getEnergyAccrued());
        assertAmount("0", loan.getActiveLoan().getXpAccrued());
    }

    @Test
    void accruedXp_usesXpTaxNotEnergyTax() {
        newService();
        Loan loan = borrowedLoan(25, 10);

        BigDecimal kept = service.accruedXp(loan, new BigDecimal("100"));

        assertAmount("75", kept);
        assertAmount("25", loan.getActiveLoan().getXpAccrued());
        assertAmount("0", loan.getActiveLoan().getEnergyAccrued());
    }

    @Test
    void flushDirtyLoans_afterTaxedAccrual_persistsLoan() {
        newService();
        Loan loan = borrowedLoan(25, 0);
        int upsertsBeforeFlush = storage.upsertCount;

        service.accruedXp(loan, new BigDecimal("100"));
        service.flushDirtyLoans();

        assertEquals(upsertsBeforeFlush + 1, storage.upsertCount);
    }

    @Test
    void flushDirtyLoans_dirtySetClears_secondFlushWritesNothing() {
        newService();
        Loan loan = borrowedLoan(25, 0);
        service.accruedXp(loan, new BigDecimal("100"));
        service.flushDirtyLoans();
        int upsertsAfterFirstFlush = storage.upsertCount;

        service.flushDirtyLoans();

        assertEquals(upsertsAfterFirstFlush, storage.upsertCount);
    }

    @Test
    void flushDirtyLoans_zeroTaxAccrual_writesNothing() {
        newService();
        Loan loan = borrowedLoan(0, 0);
        service.accruedXp(loan, new BigDecimal("100"));
        int upsertsBeforeFlush = storage.upsertCount;

        service.flushDirtyLoans();

        assertEquals(upsertsBeforeFlush, storage.upsertCount);
    }

    @Test
    void flushDirtyLoans_multipleAccrualsOnSameLoan_writesOnce() {
        newService();
        Loan loan = borrowedLoan(10, 10);
        service.accruedXp(loan, new BigDecimal("100"));
        service.accruedEnergy(loan, new BigDecimal("100"));
        int upsertsBeforeFlush = storage.upsertCount;

        service.flushDirtyLoans();

        assertEquals(upsertsBeforeFlush + 1, storage.upsertCount);
    }
}
