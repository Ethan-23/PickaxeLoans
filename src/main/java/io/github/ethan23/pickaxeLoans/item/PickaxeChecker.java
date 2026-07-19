package io.github.ethan23.pickaxeLoans.item;

import io.github.ethan23.pickaxeLoans.cosmic.model.PickaxeType;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

/**
 * Static helpers for identifying loanable pickaxes and loan-tagged items.
 *
 * <p>A loaned pickaxe is recognised purely by the loan id stored in its
 * persistent data container under {@link LoanKeys#loanKey} — no other state
 * is attached to the item itself.
 */
public class PickaxeChecker {

    /**
     * Checks whether the item can be listed as a loan: it must be a
     * recognized pickaxe type and must not already be a loaned item.
     * Messages the player when the item does not qualify.
     *
     * @param player the player attempting to list, messaged on failure
     * @param itemStack the item to check, may be null
     * @return true if the item can be listed
     */
    public static boolean checkLoanCreateRequirements(Player player, ItemStack itemStack){
        // This is where the pickaxe level and whitescroll check would be on cosmic.
        boolean canLoan = itemStack != null && PickaxeType.isPickaxeType(itemStack.getType()) && !itemStack.getItemMeta().getPersistentDataContainer().has(LoanKeys.loanKey, PersistentDataType.STRING);

        if(!canLoan){
            player.sendMessage(ColorTextBuilder.parse("<red>You cannot loan this item!"));

        }

        return canLoan;
    }

    /**
     * @param itemStack the item to check, may be null
     * @return true if the item carries a loan tag
     */
    public static boolean isLoanItem(ItemStack itemStack){
        if(itemStack == null){
            return false;
        }
        return itemStack.getPersistentDataContainer().has(LoanKeys.loanKey, PersistentDataType.STRING);
    }

    /**
     * Reads the loan id from an item's loan tag.
     *
     * @param itemStack the item to read, may be null
     * @return the loan id, or null if the item carries no loan tag
     */
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

    /**
     * Reconciles a player's inventory against their currently valid loan:
     * every loan-tagged item that does not belong to the loan they are
     * actively borrowing is removed. Called on join, on loan end, and on
     * manual return, so stale copies cannot outlive their loan. Does nothing
     * if the player is offline.
     *
     * @param loanService used to look up the player's active borrow
     * @param playerUUID the player whose inventory to reconcile
     */
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
            player.sendMessage(ColorTextBuilder.parse("<yellow>Your loan has been returned."));
        }

    }

    /**
     * Finds the slot holding an item exactly equal to the given stack.
     *
     * @param inventory the inventory to search
     * @param itemStack the item to look for, may be null
     * @return the first matching slot, or -1 if not found
     */
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
