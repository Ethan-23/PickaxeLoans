package io.github.ethan23.pickaxeLoans.item;

import io.github.ethan23.pickaxeLoans.cosmic.PickaxeType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.util.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class PickaxeChecker {

    public static boolean checkLoanCreateRequirements(ItemStack itemStack){
        //Whitescroll Check
        return itemStack != null && PickaxeType.isPickaxeType(itemStack.getType()) && !itemStack.getItemMeta().getPersistentDataContainer().has(LoanKeys.loanKey, PersistentDataType.STRING);

    }

    public static boolean isLoanItem(ItemStack itemStack){
        if(itemStack == null){
            return false;
        }
        return itemStack.getPersistentDataContainer().has(LoanKeys.loanKey, PersistentDataType.STRING);
    }

    public static UUID getLoanUUID(ItemStack itemStack) {
        if(itemStack == null || itemStack.getItemMeta() == null) {
            return null;
        }

        String uuidString = itemStack.getItemMeta().getPersistentDataContainer().get(LoanKeys.loanKey, PersistentDataType.STRING);

        if(uuidString == null){
            return null;
        }

        return UUID.fromString(uuidString);

    }

    public static void removeLoan(LoanService loanService, UUID playerUUID) {

        Player player = Bukkit.getPlayer(playerUUID);

        if(player == null){
            return;
        }

        Inventory inventory = player.getInventory();

        Loan borrowed = loanService.getBorrowersLoan(playerUUID);
        UUID validLoanUUID = borrowed != null ? borrowed.getLoanUUID() : null;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null) {
                continue;
            }

            UUID pickaxeLoanUUID = getLoanUUID(itemStack);
            if (pickaxeLoanUUID == null) {
                continue;
            }

            if (pickaxeLoanUUID.equals(validLoanUUID)) {
                continue;
            }

            inventory.setItem(slot, null);
            player.sendMessage(ComponentBuilder.parse("<yellow>Your loan has been returned."));
        }

    }

    public static int findMatching(Inventory inventory, ItemStack itemStack){

        if(itemStack == null) {
            return -1;
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (Objects.equals(inventory.getItem(slot), itemStack)) {
                return slot;
            }
        }
        return -1;
    }
}
