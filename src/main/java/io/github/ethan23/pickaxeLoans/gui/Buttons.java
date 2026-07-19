package io.github.ethan23.pickaxeLoans.gui;

import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.util.ItemBuilder;
import org.bukkit.Material;

public final class Buttons {

    private Buttons() {

    }

    public static InventoryButton guide() {
        return new InventoryButton(ItemBuilder.of(Material.BOOK, "<bold><yellow>Guide"), event -> {
        });
    }

    public static InventoryButton close() {
        return new InventoryButton(ItemBuilder.of(Material.BARRIER, "<red>Close"), e -> {
            e.getWhoClicked().closeInventory();
        });
    }

    public static InventoryButton confirm(Runnable action) {
        return new InventoryButton(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE, "<green>Confirm"), e -> {
            action.run();
        });
    }

    public static InventoryButton deny(Runnable action) {
        return new InventoryButton(ItemBuilder.of(Material.RED_STAINED_GLASS_PANE, "<red>Deny"), e -> {
            action.run();
        });
    }


    public static InventoryButton activeLoanListed(Loan loan, Runnable action) {
        return new InventoryButton(
                LoanLore.of(loan)
                        .divider()
                        .state()
                        .price()
                        .loanTime()
                        .xpTax()
                        .energyTax()
                        .offerExpires()
                        .divider()
                        .blank()
                        .exit()
                        .applyTo(loan.getPickaxe()), event -> {
            if (event.getClick().isRightClick()) {
                action.run();
            }
        }
        );
    }

    public static InventoryButton expiredLoanListed(Loan loan, Runnable action) {
        return new InventoryButton(
                LoanLore.of(loan)
                        .divider()
                        .state()
                        .price()
                        .loanTime()
                        .xpTax()
                        .energyTax()
                        .divider()
                        .blank()
                        .exit()
                        .applyTo(loan.getPickaxe()), event -> {
            action.run();
        }
        );
    }

    public static InventoryButton activeLoanBorrowed(Loan loan, Runnable action) {
        return new InventoryButton(
                LoanLore.of(loan)
                        .divider()
                        .state()
                        .remainingLoanTime()
                        .xpTaxResult()
                        .energyTaxResult()
                        .divider()
                        .applyTo(loan.getPickaxe()), event -> {
            action.run();
        });
    }

    public static InventoryButton currentLoanAgreement(Loan loan, Runnable action) {

        if (loan == null) {
            return new InventoryButton(ItemBuilder.of(Material.WRITABLE_BOOK, "<bold><yellow>Current Loan Agreement", "", "<yellow>None"), event -> {
            });
        } else {
            return new InventoryButton(
                    LoanLore.of(loan)
                            .remainingLoanTime()
                            .xpTax()
                            .energyTax()
                            .blank()
                            .exit()
                            .applyTo(ItemBuilder.of(Material.WRITABLE_BOOK, "<bold><yellow>Current Loan Agreement")), event -> {
                if (!event.getClick().isRightClick()) {
                    return;
                }
                action.run();
            });
        }
    }

    public static InventoryButton activeLoanReturned(Loan loan, Runnable action) {
        return new InventoryButton(
                LoanLore.of(loan)
                        .divider()
                        .state()
                        .remainingLoanTime()
                        .xpTaxResult()
                        .energyTaxResult()
                        .divider()
                        .applyTo(loan.getPickaxe()), event -> {
            action.run();
        });
    }


}
