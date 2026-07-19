package io.github.ethan23.pickaxeLoans.gui.menu;

import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import io.github.ethan23.pickaxeLoans.gui.*;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanState;
import io.github.ethan23.pickaxeLoans.util.ComponentBuilder;
import io.github.ethan23.pickaxeLoans.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class LoanListingMenu extends InventoryGUI {

    private static final Component INVENTORY_TITLE = ComponentBuilder.parse("Loan Listing");
    private static final int INVENTORY_SIZE = 6 * 9;
    private static final int LOAN_SLOTS = 5 * 9;
    private static final int ACTIVE_LOAN_SLOT = 45;
    private static final int CURRENT_LOAN_AGREEMENT_SLOT = 47;
    private static final int PREV_PAGE_SLOT = 48;
    private static final int REFRESH_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 50;
    private static final int GUIDE_SLOT = 53;

    private PlayerInputListener playerInputListener;
    private int page;
    private final Player player;
    private final UUID playerUUID;

    private final LoanService loanService;

    public LoanListingMenu(Player player, LoanService loanService, PlayerInputListener playerInputListener) {
        super(INVENTORY_SIZE, INVENTORY_TITLE);
        this.loanService = loanService;
        this.playerInputListener = playerInputListener;
        this.page = 1;
        this.player = player;
        this.playerUUID = player.getUniqueId();

        addButton(ACTIVE_LOAN_SLOT, new InventoryButton(ItemBuilder.of(Material.ENDER_CHEST, "<bold><yellow>Active Loans", "<gray>Click here to view and collect all the loans you created", "", "<bold><yellow>" + loanService.getLenderLoans(this.playerUUID).size() + " Item(s)"), event -> {
            player.openInventory(new ActiveLoansMenu(getInventory(), player, loanService).getInventory());
        }));

        buildCurrentLoanAgreementButton();

        addButton(PREV_PAGE_SLOT, new InventoryButton(ItemBuilder.of(Material.ARROW, "<red>Previous Page"), event -> {
            if(page > 1){
                page--;
                reloadPage();
            }
        }));
        addButton(REFRESH_SLOT, new InventoryButton(ItemBuilder.of(Material.CHEST, "<yellow>Refresh"), event -> {
            reloadPage();
        }));
        addButton(NEXT_PAGE_SLOT, new InventoryButton(ItemBuilder.of(Material.ARROW, "<red>Next Page"), event -> {
            if(page * LOAN_SLOTS <= loanService.getListedLoans().size() - 1) {
                page++;
                reloadPage();
            }
        }));
        addButton(GUIDE_SLOT, Buttons.guide());

        loadLoanList();

    }

    private void buildCurrentLoanAgreementButton() {
        removeButton(CURRENT_LOAN_AGREEMENT_SLOT);

        Loan loan = loanService.getBorrowersLoan(this.playerUUID);

        addButton(CURRENT_LOAN_AGREEMENT_SLOT, Buttons.currentLoanAgreement(loan, () -> {


            if(loan == null){
                player.sendMessage(ComponentBuilder.parse("<red>You are currently not borrowing a pickaxe!"));
                return;
            }

            player.sendMessage(ComponentBuilder.parse("<yellow>You have returned your loan."));
            loanService.returnLoan(loan.getLoanUUID());
            PickaxeChecker.removeLoan(loanService, this.playerUUID);
            buildCurrentLoanAgreementButton();
            decorate();
        }));
    }

    private void clearLoanList(){
        for(int i = 0; i < LOAN_SLOTS; i++){
            getInventory().setItem(i, new ItemStack(Material.AIR));
            removeButton(i);
        }
    }

    private void loadLoanList(){
        List<Loan> loanList = loanService.getListedLoans().reversed();

        if(loanList.isEmpty())
            return;

        int i = 0;

        for(int loans = LOAN_SLOTS * (page - 1); loans < LOAN_SLOTS * page; loans++) {
            if (loans >= loanList.size())
                return;
            Loan loan = loanList.get(loans);
            if (loan.getLoanState() != LoanState.LISTED) {
                continue;
            }
            addButton(i, new InventoryButton(
                    LoanLore.of(loan)
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
                            .applyTo(loan.getPickaxe()),
                    event -> {
                        if(!loan.getLenderUUID().equals(this.playerUUID)) {
                            player.openInventory(new LoanPurchaseMenu(getInventory(), player, loanService, loan).getInventory());
                        }
                    })
            );
            i++;
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);

        if(event.getClickedInventory() != event.getView().getBottomInventory()){
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if(PickaxeChecker.checkLoanCreateRequirements(clickedItem)){
            player.openInventory(new LoanCreateMenu(playerInputListener, loanService, player, clickedItem, event.getSlot()).getInventory());
        }
    }

    private void reloadPage(){
        clearLoanList();
        loadLoanList();
        decorate();
    }

}
