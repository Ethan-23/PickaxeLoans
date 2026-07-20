package io.github.ethan23.pickaxeLoans.gui.menu;

import io.github.ethan23.pickaxeLoans.config.LoanConfig;
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

import static io.github.ethan23.pickaxeLoans.util.ColorTextBuilder.parse;

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
    private static final long MILLIS_PER_MINUTE   = 60_000L;

    private final LoanDeal loanDeal;
    private ItemStack pickaxeItem;
    private int pickaxeSlot;
    private final Player player;
    private final LoanService loanService;
    private final LoanConfig config;
    private final PlayerInputListener playerInputListener;

    public LoanCreateMenu(PlayerInputListener playerInputListener, LoanService loanService, Player player, ItemStack pickaxeItem, int slot) {
        super(INVENTORY_SIZE, INVENTORY_TITLE);
        this.pickaxeItem = pickaxeItem;
        this.pickaxeSlot = slot;
        this.player = player;
        this.loanService = loanService;
        this.config = loanService.getConfig();
        this.playerInputListener = playerInputListener;
        this.loanDeal = new LoanDeal();
        clampDealToBounds();



        addButton(GUIDE_SLOT, Buttons.guide(
                "<gray>Set the terms of your loan here, then press <green>Confirm <gray>to put your pickaxe on the market.",
                "",
                "<yellow>Cost <gray>is the one-time price a borrower pays up front. Right-Click it to switch between money and energy.",
                "",
                "<yellow>Loan Time <gray>is how long the borrower keeps your pickaxe before it returns to you automatically.",
                "",
                "<yellow>Taxes <gray>are your ongoing payout: the percent of the XP and energy the borrower earns with your pickaxe that goes to you instead.",
                "",
                "<gray>Clicking a value closes this menu and asks for the new amount in chat. Numbers like <yellow>10k <gray>and <yellow>1m <gray>work, or type <red>cancel <gray>to come back."
        ));

        renderClickable();
    }

    /**
     * Forces the deal's starting values into the configured bounds, so the
     * menu never opens pre-loaded with terms the server owner has
     * disallowed (e.g. a 30-minute default when the configured maximum
     * duration is 20).
     */
    private void clampDealToBounds() {
        loanDeal.setUpfrontCost(Math.clamp(loanDeal.getUpfrontCost(), config.getUpfrontCostMin(), config.getUpfrontCostMax()));
        long durationMinutes = loanDeal.getLoanDurationMillis() / MILLIS_PER_MINUTE;
        loanDeal.setLoanDurationMillis(Math.clamp(durationMinutes, config.getDurationMinutesMin(), config.getDurationMinutesMax()) * MILLIS_PER_MINUTE);
        loanDeal.setXpTaxPercent(Math.clamp(loanDeal.getXpTaxPercent(), config.getXpTaxMin(), config.getXpTaxMax()));
        loanDeal.setEnergyTaxPercent(Math.clamp(loanDeal.getEnergyTaxPercent(), config.getEnergyTaxMin(), config.getEnergyTaxMax()));
    }

    private void renderClickable(){

        addButton(PICKAXE_PREVIEW_SLOT, new InventoryButton(pickaxeItem, event -> {}));

        boolean isMoney = loanDeal.getCostType() == CostType.MONEY;
        String cost = NumberConversions.formattedNumberDisplay(loanDeal.getUpfrontCost());
        String costMin = NumberConversions.formattedNumberDisplay((double) config.getUpfrontCostMin());
        String costMax = NumberConversions.formattedNumberDisplay((double) config.getUpfrontCostMax());
        String costRange = isMoney
                ? "<green>$" + costMin + " <gray>- <green>$" + costMax
                : "<white>" + costMin + " <gray>- <white>" + costMax;

        addButton(UPFRONT_COST_SLOT, new InventoryButton(
                ItemBuilder.of(
                        isMoney ? Material.PAPER : Material.LIGHT_BLUE_DYE,
                        "<bold><yellow>Loan Cost",
                        isMoney ? "<green>$" + cost : "<white>" + cost + " <aqua>Cosmic Energy",
                        "",
                        "<gray>Range: " + costRange,
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
                        promptForNumber(config.getUpfrontCostMin(), config.getUpfrontCostMax(), loanDeal::setUpfrontCost);
                    }
                }
        ));

        addButton(LOAN_DURATION_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.CLOCK,
                        "<bold><yellow>Total Loan Time",
                        "<white>" + NumberConversions.timeFromMillis(loanDeal.getLoanDurationMillis()),
                        "",
                        "<gray>Range: <white>" + config.getDurationMinutesMin() + " <gray>- <white>" + config.getDurationMinutesMax() + " <gray>minutes",
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                ),
                e -> promptForNumber(config.getDurationMinutesMin(), config.getDurationMinutesMax(), value -> loanDeal.setLoanDurationMillis(value * MILLIS_PER_MINUTE))
        ));

        addButton(XP_TAX_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.POTION,
                        "<bold><yellow>Loan XP Tax",
                        "<white>" + loanDeal.getXpTaxPercent() + "%",
                        "",
                        "<gray>Range: <white>" + config.getXpTaxMin() + "% <gray>- <white>" + config.getXpTaxMax() + "%",
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                ),
                e -> promptForNumber(config.getXpTaxMin(), config.getXpTaxMax(), value -> loanDeal.setXpTaxPercent((int) value))
        ));

        addButton(ENERGY_TAX_SLOT, new InventoryButton(
                ItemBuilder.of(
                        Material.LIGHT_BLUE_DYE,
                        "<bold><yellow>Loan Energy Tax",
                        "<white>" + loanDeal.getEnergyTaxPercent() + "%",
                        "",
                        "<gray>Range: <white>" + config.getEnergyTaxMin() + "% <gray>- <white>" + config.getEnergyTaxMax() + "%",
                        "",
                        "<yellow>Left-Click <gray>to edit value"
                )
                ,
                e -> promptForNumber(config.getEnergyTaxMin(), config.getEnergyTaxMax(), value -> loanDeal.setEnergyTaxPercent((int) value))

        ));

        addButton(CONFIRM_BUTTON_SLOT, new InventoryButton(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE, "<green>Confirm"), e -> {

            int foundSlot = PickaxeChecker.findMatching(player.getInventory(), pickaxeItem);
            if (foundSlot == -1) {
                player.sendMessage(parse("<red>That pickaxe is no longer in your inventory."));
                player.closeInventory();
                return;
            }
            this.pickaxeSlot = foundSlot;
            LoanResult loanResult = loanService.createListing(new Loan(pickaxeItem, player.getUniqueId(), loanDeal, config.getListingExpiryMillis()));
            if(loanResult == LoanResult.MAX_LOANS){
                player.sendMessage(parse("<red>You can only create " + config.getMaxListingsPerPlayer() + " loans at a time!"));
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

        if(clickedItem == null){
            return;
        }

        if(!PickaxeChecker.checkLoanCreateRequirements(player, clickedItem)){
            return;
        }
        this.pickaxeItem = clickedItem.clone();
        this.pickaxeSlot = event.getSlot();
        renderClickable();
        player.updateInventory();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
    }
}
