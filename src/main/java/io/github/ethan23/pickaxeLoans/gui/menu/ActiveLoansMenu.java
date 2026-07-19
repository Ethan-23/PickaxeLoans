package io.github.ethan23.pickaxeLoans.gui.menu;

import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.gui.Buttons;
import io.github.ethan23.pickaxeLoans.gui.InventoryButton;
import io.github.ethan23.pickaxeLoans.gui.InventoryGUI;
import io.github.ethan23.pickaxeLoans.model.*;
import io.github.ethan23.pickaxeLoans.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

import static io.github.ethan23.pickaxeLoans.util.ComponentBuilder.parse;

public class ActiveLoansMenu extends InventoryGUI {

    private static final Component INVENTORY_TITLE = parse("Your Loans");
    private static final int INVENTORY_SIZE = 6 * 9;
    private static final int LOAN_SLOTS = 5 * 9;
    private static final int BACK_SLOT = 45;
    private static final int GUIDE_SLOT = 53;

    private final Player player;
    private final LoanService loanService;
    private final CosmicPlayerService cosmicPlayerService;

    public ActiveLoansMenu(Inventory prevInventory, Player player, LoanService loanService, CosmicPlayerService cosmicPlayerService) {
        super(INVENTORY_SIZE, INVENTORY_TITLE);
        this.loanService = loanService;
        this.cosmicPlayerService = cosmicPlayerService;
        this.player = player;

        addButton(BACK_SLOT, new InventoryButton(ItemBuilder.of(Material.CHEST, "<bold><yellow>Back to loans"), e -> {
            player.openInventory(prevInventory);
        }));

        addButton(GUIDE_SLOT, Buttons.guide());

        loadLoanList();
    }

    private void loadLoanList() {

        int i = 0;
        for (Loan loan : loanService.getLenderLoans(player.getUniqueId()).reversed()) {

            if (loan.getLoanState() == LoanState.LISTED) {
                addListedButton(i, loan);
            } else if (loan.getLoanState() == LoanState.BORROWED) {
                addButton(i, Buttons.activeLoanBorrowed(loan, () -> {}));
            } else if (loan.getLoanState() == LoanState.RETURNED) {
                addReturnedButton(i, loan);
            } else if (loan.getLoanState() == LoanState.CANCELLED || loan.getLoanState() == LoanState.EXPIRED) {
                addCollectableButton(i, loan);
            }
            i++;
        }

    }

    private void addListedButton(int slot, Loan loan){
        addButton(slot, Buttons.activeLoanListed(loan, () -> {
            LoanResult result = loanService.cancel(loan.getLoanUUID());
            switch (result) {
                case SUCCESS -> {
                    reloadPage();
                    player.sendMessage(parse("<yellow>Loan listing has been canceled."));
                }
                case NOT_LISTED -> {
                    player.sendMessage(parse("<red>That loan is no longer listed!"));
                }
                case NOT_FOUND -> {
                    reloadPage();
                }
                default -> {
                }
            }
        }));
    }

    private void addReturnedButton(int i, Loan loan) {
        addButton(i, Buttons.activeLoanReturned(loan, () -> {
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(parse("<red>You do not have enough inventory space!"));
                return;
            }

            loanService.deleteLoan(loan);
            reloadPage();
            player.getInventory().addItem(loan.getPickaxe());
            player.sendMessage(parse("<yellow>You have claimed your returned loan and fees."));

            BigDecimal totalEnergyAccrued = loan.getActiveLoan().getEnergyAccrued();
            BigDecimal totalExperienceAccrued = loan.getActiveLoan().getXpAccrued();

            UUID playerUUID = player.getUniqueId();

            cosmicPlayerService.addEnergy(playerUUID, totalEnergyAccrued);
            cosmicPlayerService.addExperience(playerUUID, totalExperienceAccrued);

            player.sendMessage(parse("+ " + totalEnergyAccrued + " Cosmic Energy"));
            player.sendMessage(parse("+ " + totalExperienceAccrued + " Experience"));
        }));
    }

    private void addCollectableButton(int i, Loan loan) {
        addButton(i, Buttons.expiredLoanListed(loan, () -> {
            if (loan.getLoanState() == LoanState.CANCELLED || loan.getLoanState() == LoanState.EXPIRED) {

                player.getInventory().addItem(loan.getPickaxe());
                loanService.deleteLoan(loan);
                reloadPage();
                player.sendMessage(parse("<yellow>Loan listing has been claimed."));
            }
        }));
    }

    private void clearLoanList() {
        for (int i = 0; i < LOAN_SLOTS; i++) {
            getInventory().setItem(i, new ItemStack(Material.AIR));
            removeButton(i);
        }
    }

    private void reloadPage() {
        clearLoanList();
        loadLoanList();
        decorate();
    }

}
