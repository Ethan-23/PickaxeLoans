package io.github.ethan23.pickaxeLoans.gui;

import io.github.ethan23.pickaxeLoans.model.CostType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.model.LoanDeal;
import io.github.ethan23.pickaxeLoans.model.LoanState;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import io.github.ethan23.pickaxeLoans.util.NumberConversions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class LoanLore {

    private final List<Component> lines = new ArrayList<>();
    private final Loan loan;
    private final LoanDeal deal;

    private LoanLore(Loan loan) {
        this.loan = loan;
        this.deal = loan.getLoanDeal();
    }

    public static LoanLore of(Loan loan) {
        return new LoanLore(loan);
    }

    public LoanLore divider() {
        lines.add(ColorTextBuilder.parse("<gray>-------------------------"));
        return this;
    }

    public LoanLore blank() {
        lines.add(Component.empty());
        return this;
    }

    public LoanLore callToAction() {
        lines.add(ColorTextBuilder.parse("<bold><yellow>Click item to loan!"));
        return this;
    }

    public LoanLore price() {
        String cost = NumberConversions.formattedNumberDisplay(deal.getUpfrontCost());
        String value = deal.getCostType() == CostType.MONEY
                ? "$" + cost
                : cost + " cosmic energy";
        lines.add(ColorTextBuilder.parse("<gold>Price: <yellow>" + value));
        return this;
    }

    public LoanLore lender() {
        var p = Bukkit.getOfflinePlayer(loan.getLenderUUID());
        String name = p.getName() == null ? "Unknown" : p.getName();
        lines.add(ColorTextBuilder.parse("<gold>Lender: <yellow>" + name));
        return this;
    }

    public LoanLore loanTime() {
        lines.add(ColorTextBuilder.parse("<gold>Loan Time: <yellow>" + NumberConversions.timeFromMillis(deal.getLoanDurationMillis())));
        return this;
    }

    public LoanLore xpTax() {
        lines.add(ColorTextBuilder.parse("<gold>Xp Tax: <yellow>" + deal.getXpTaxPercent() + "%"));
        return this;
    }

    public LoanLore xpTaxResult() {
        lines.add(ColorTextBuilder.parse("<gold>Xp Tax: <yellow>" + deal.getXpTaxPercent() + "%" + " (" + NumberConversions.formattedNumberDisplay(loan.getActiveLoan().getXpAccrued()) + ")"));
        return this;
    }

    public LoanLore energyTaxResult() {
        lines.add(ColorTextBuilder.parse("<gold>Energy Tax: <yellow>" + deal.getEnergyTaxPercent() + "%" + " (" + NumberConversions.formattedNumberDisplay(loan.getActiveLoan().getEnergyAccrued()) + ")"));
        return this;
    }

    public LoanLore energyTax() {
        lines.add(ColorTextBuilder.parse("<gold>Energy Tax: <yellow>" + deal.getEnergyTaxPercent() + "%"));
        return this;
    }

    public LoanLore offerExpires() {
        long remaining = loan.getListingExpiresAt() - System.currentTimeMillis();
        lines.add(ColorTextBuilder.parse("<gold>Offer Expires: <yellow>" + NumberConversions.timeFromMillis(remaining)));
        return this;
    }

    public LoanLore state() {
        lines.add(ColorTextBuilder.parse("<gold>Loan State: <yellow>" + loan.getLoanState().toString()));
        return this;
    }

    public LoanLore exit() {

        if(loan.getLoanState() == LoanState.LISTED){
            lines.add(ColorTextBuilder.parse("<gray>Right-Click to <red>CANCEL <gray>Loan."));
        }else if(loan.getLoanState() == LoanState.CANCELLED || loan.getLoanState() == LoanState.EXPIRED){
            lines.add(ColorTextBuilder.parse("<gray>Right-Click to <green>CLAIM <gray>Loan."));
        }else if(loan.getLoanState() == LoanState.BORROWED){
            lines.add(ColorTextBuilder.parse("<gray>Right-Click to <gold>RETURN <gray>Loan."));
        }

        return this;
    }

    public LoanLore remainingLoanTime() {
        lines.add(ColorTextBuilder.parse("<gold>Remaining Loan Time: <yellow>" + NumberConversions.timeFromMillis(loan.getActiveLoan().getEndsAt() - System.currentTimeMillis())));
        return this;
    }

    public ItemStack applyTo(ItemStack itemStack) {
        ItemStack copy = itemStack.clone();
        ItemMeta meta = copy.getItemMeta();

        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.addAll(lines);

        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }
}
