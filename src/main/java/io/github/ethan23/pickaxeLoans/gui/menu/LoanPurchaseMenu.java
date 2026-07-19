package io.github.ethan23.pickaxeLoans.gui.menu;

import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.gui.Buttons;
import io.github.ethan23.pickaxeLoans.gui.InventoryButton;
import io.github.ethan23.pickaxeLoans.gui.InventoryGUI;
import io.github.ethan23.pickaxeLoans.gui.LoanLore;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanResult;
import io.github.ethan23.pickaxeLoans.util.ComponentBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.github.ethan23.pickaxeLoans.util.ComponentBuilder.parse;

public class LoanPurchaseMenu extends InventoryGUI {

    private static final Component INVENTORY_TITLE = ComponentBuilder.parse("Purchase Loan: Are you sure?");
    private static final int INVENTORY_SIZE = 9;
    private static final List<Integer> CONFIRM_SLOTS = List.of(0, 1, 2, 3);
    private static final List<Integer> DENY_SLOTS = List.of(5, 6, 7, 8);
    private static final Integer PICKAXE_SLOT = 4;

    public LoanPurchaseMenu(Inventory prevInventory, Player player, LoanService loanService, Loan loan, CosmicPlayerService cosmicPlayerService) {
        super(INVENTORY_SIZE, INVENTORY_TITLE);

        UUID playerUUID = player.getUniqueId();

        for(int i : CONFIRM_SLOTS){
            addButton(i, Buttons.confirm(() -> {

                CostType costType = loan.getLoanDeal().getCostType();

                if(player.getInventory().firstEmpty() == -1){
                    player.sendMessage(ComponentBuilder.parse("<red>You must have an open inventory space to loan!"));
                    return;
                }

                if(costType == CostType.ENERGY){
                    BigDecimal energy = cosmicPlayerService.getEnergy(playerUUID);
                    if(energy.compareTo(BigDecimal.valueOf(loan.getLoanDeal().getUpfrontCost())) < 0){
                        player.sendMessage(ComponentBuilder.parse("<red>You do not have enough energy to borrow this pickaxe!"));
                        return;
                    }
                    cosmicPlayerService.removeEnergy(playerUUID, energy);
                }
                //Money check would go here if I implemented an economy :)

                LoanResult loanResult = loanService.borrow(playerUUID, loan.getLoanUUID());
                switch (loanResult){
                    case LENDERS_LOAN -> {
                        player.sendMessage(parse("<red>You cannot loan from yourself. If you wish to cancel go to your active loans!"));
                        player.openInventory(prevInventory);
                    }
                    case ALREADY_BORROWING -> {
                        player.sendMessage(parse("<red>You are already borrowing a loan!"));
                        player.openInventory(prevInventory);
                    }
                    case NOT_FOUND ->{
                        player.sendMessage(parse("<red>Invalid Loan Attempt!"));
                        player.openInventory(prevInventory);
                    }
                    case NOT_LISTED ->{
                        player.sendMessage(parse("<red>Loan is no longer listed!"));
                        player.openInventory(prevInventory);
                    }
                    case SUCCESS ->{
                        player.sendMessage(parse("<yellow>You have accepted the loan from " + Bukkit.getOfflinePlayer(loan.getLenderUUID()).getName()));
                        player.getInventory().addItem(loan.getLoanPickaxe());
                        player.closeInventory();
                    }
                }

            }));
        }

        for(int i : DENY_SLOTS){
            addButton(i, Buttons.deny( () -> {
                player.openInventory(prevInventory);
            }));
        }

        addButton(PICKAXE_SLOT, new InventoryButton(LoanLore.of(loan)
                .divider()
                .callToAction()
                .blank()
                .price()
                .lender()
                .loanTime()
                .xpTax()
                .energyTax()
                .offerExpires()
                .divider()
                .applyTo(loan.getPickaxe())
        ));
    }
}
