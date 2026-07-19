package io.github.ethan23.pickaxeLoans.events;

import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

/**
 * Keeps loaned pickaxes inside the borrower's own inventory.
 *
 * <p>A loaned pickaxe is a tagged copy of the escrow original — if it
 * could be stashed in a chest or any other container it would outlive the
 * loan and duplicate the lender's pickaxe. Each handler below blocks one
 * smuggling route into a container view. The player's own 2x2 crafting view
 * ({@link InventoryType#CRAFTING}) is exempt, since items there never leave
 * the player.
 */
public class InventoryMoveItemListener implements Listener {

    /**
     * Blocks the click-based routes into a container: shift-clicking a
     * loaned item out of the player inventory, dropping a held loaned item
     * into the top inventory, and hotbar-swapping a loaned item into the
     * top inventory.
     */
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getType() == InventoryType.CRAFTING){
            return;
        }

        boolean clickedTop = event.getClickedInventory() == top;

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && !clickedTop && PickaxeChecker.isLoanItem(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        if (clickedTop && PickaxeChecker.isLoanItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (clickedTop && event.getHotbarButton() != -1
                && PickaxeChecker.isLoanItem(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()))) {
            event.setCancelled(true);
        }
    }

    /** Blocks dragging a held loaned item across any slot of a container view. */
    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!PickaxeChecker.isLoanItem(event.getOldCursor())){
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING
                && event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
        }
    }

}
