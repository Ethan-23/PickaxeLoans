package io.github.ethan23.pickaxeLoans.gui.menu;

import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import io.github.ethan23.pickaxeLoans.gui.Buttons;
import io.github.ethan23.pickaxeLoans.gui.InventoryButton;
import io.github.ethan23.pickaxeLoans.gui.InventoryGUI;
import io.github.ethan23.pickaxeLoans.gui.PlayerInputListener;
import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import io.github.ethan23.pickaxeLoans.model.LoanResult;
import io.github.ethan23.pickaxeLoans.util.ItemBuilder;
import io.github.ethan23.pickaxeLoans.util.NumberConversions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.LongConsumer;

import static io.github.ethan23.pickaxeLoans.util.ComponentBuilder.parse;

public class LoanCreateMenu extends InventoryGUI {

    private static final int INVENTORY_SIZE = 6 * 9;
    private static final Component INVENTORY_TITLE = parse("Loan Create");
    private static final int PICKAXE_PREVIEW_SLOT = 13;
    private static final int UPFRONT_COST_SLOT = 28;
    private static final int LOAN_DURATION_SLOT = 30;
    private static final int XP_TAX_SLOT = 32;
    private static final int ENERGY_TAX_SLOT = 34;
    private static final int CONFIRM_BUTTON_SLOT = 49;
    private static final int GUIDE_SLOT = 53;
    private static final long MIN_UPFRONT_COST   = 0L;
    private static final long MAX_UPFRONT_COST   = 1_000_000_000L;
    private static final long MIN_LOAN_MINUTES    = 10;
    private static final long MAX_LOAN_MINUTES    = 60;
    private static final int  MIN_TAX_PERCENT     = 0;
    private static final int  MAX_TAX_PERCENT     = 100;
    private static final long MILLIS_PER_MINUTE   = 60_000L;

    private final LoanDeal loanDeal;
    private ItemStack pickaxeItem;
    private int pickaxeSlot;
    private final Player player;
    private final LoanService loanService;
    private final PlayerInputListener playerInputListener;

    public LoanCreateMenu(PlayerInputListener playerInputListener, LoanService loanService, Player player, ItemStack pickaxeItem, int slot) {
        super(INVENTORY_SIZE, INVENTORY_TITLE);
        this.pickaxeItem = pickaxeItem;
        this.pickaxeSlot = slot;
        this.loanDeal = new LoanDeal();
        this.player = player;
        this.loanService = loanService;
        this.playerInputListener = playerInputListener;



        addButton(GUIDE_SLOT, Buttons.guide());

        renderClickable();
    }

    private void renderClickable(){

        addButton(PICKAXE_PREVIEW_SLOT, new InventoryButton(pickaxeItem, event -> {}));

        boolean isMoney = loanDeal.getCostType() == CostType.MONEY;
        String cost = NumberConversions.formattedNumberDisplay(loanDeal.getUpfrontCost());

        addButton(UPFRONT_COST_SLOT, new InventoryButton(
                ItemBuilder.of(
                        isMoney ? Material.PAPER : Material.LIGHT_BLUE_DYE,
                        "<bold><yellow>Loan Cost",
                        isMoney ? "<green>$" + cost : "<white>" + cost + " <aqua>Cosmic Energy",
                        "",
                        "<yellow>Left-Click <gray>to edit value",
                        "<yellow>Right-Click <gray>to swap type"
                ),
                e -> {
                    if (e.isRightClick()) {
                        if (loanDeal.getCostType() == CostType.MONEY) {
                            loanDeal.setCostType(CostType.ENERGY);
                        } else {
                            loanDeal.setCostType(CostType.MONEY);
                        }
                        renderClickable();
                        player.updateInventory();
                    } else if (e.isLeftClick()) {
                        promptForNumber(MIN_UPFRONT_COST, MAX_UPFRONT_COST, loanDeal::setUpfrontCost);
                    }
                }
        ));

        addButton(LOAN_DURATION_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.CLOCK,
                        "<bold><yellow>Total Loan Time",
                        "<white>" + NumberConversions.timeFromMillis(loanDeal.getLoanDurationMillis()),
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                ),
                e -> promptForNumber(MIN_LOAN_MINUTES,  MAX_LOAN_MINUTES,value -> loanDeal.setLoanDurationMillis(value * MILLIS_PER_MINUTE))
        ));

        addButton(XP_TAX_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.POTION,
                        "<bold><yellow>Loan XP Tax",
                        "<white>" + loanDeal.getXpTaxPercent() + "%",
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                ),
                e -> promptForNumber(MIN_TAX_PERCENT, MAX_TAX_PERCENT, value -> loanDeal.setXpTaxPercent((int) value))
        ));

        addButton(ENERGY_TAX_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.LIGHT_BLUE_DYE,
                        "<bold><yellow>Loan Energy Tax",
                        "<white>" + loanDeal.getEnergyTaxPercent() + "%",
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                )
                ,
                e -> promptForNumber(MIN_TAX_PERCENT, MAX_TAX_PERCENT, value -> loanDeal.setEnergyTaxPercent((int) value))

        ));

        addButton(CONFIRM_BUTTON_SLOT, new InventoryButton(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE, "<green>Confirm"), e -> {

            int foundSlot = PickaxeChecker.findMatching(player.getInventory(), pickaxeItem);
            if (foundSlot == -1) {
                player.sendMessage(parse("<red>That pickaxe is no longer in your inventory."));
                player.closeInventory();
                return;
            }
            this.pickaxeSlot = foundSlot;
            LoanResult loanResult = loanService.createListing(new Loan(pickaxeItem, player.getUniqueId(), loanDeal));
            if(loanResult == LoanResult.MAX_LOANS){
                player.sendMessage(parse("<red>You can only create 3 loans at a time!"));
                return;
            } else if(loanResult == LoanResult.DUPLICATE_LOAN){
                player.sendMessage(parse("<red>There was an error creating your loan!"));
                return;
            }
            player.getInventory().setItem(pickaxeSlot, null);
            player.sendMessage(parse("<yellow>Loan has been successfully listed."));
            player.closeInventory();
        }));

        decorate();
    }

    private void promptForNumber(long min, long max, LongConsumer callback){
        player.closeInventory();
        playerInputListener.requestInput(player, raw ->{

            if (raw.equalsIgnoreCase("cancel")) {
                player.openInventory(getInventory());
                return;
            }

            double value = -1;

            if(NumberConversions.hasSuffix(raw)){
                value = NumberConversions.suffixToNumber(raw);
            }

            if(value == -1){
                try {
                    value = Long.parseLong(raw.trim());
                } catch (NumberFormatException ex) {
                    player.sendMessage(parse("<red>Please enter a whole number. Example: 10, 100k, 10m"));
                    promptForNumber(min, max, callback);
                    return;
                }
            }

            if (value < min || value > max) {
                player.sendMessage(parse("<red>Enter a value between " + min + " and " + max + "."));
                promptForNumber(min, max, callback);
                return;
            }

            callback.accept((long) value);
            renderClickable();
            player.openInventory(getInventory());
        });
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);

        if(event.getClickedInventory() != event.getView().getBottomInventory() || event.getSlot() == pickaxeSlot){
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if(PickaxeChecker.checkLoanCreateRequirements(clickedItem)){
            this.pickaxeItem = clickedItem.clone();
            this.pickaxeSlot = event.getSlot();
            renderClickable();
            player.updateInventory();
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
    }
}
