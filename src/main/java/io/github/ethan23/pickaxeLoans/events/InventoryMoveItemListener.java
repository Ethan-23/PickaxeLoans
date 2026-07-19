package io.github.ethan23.pickaxeLoans.events;

import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

public class InventoryMoveItemListener implements Listener {

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
