package io.github.ethan23.pickaxeLoans.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getInventory().getHolder() instanceof InventoryHandler handler) {
            event.setCancelled(true);
            handler.onClick(event);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event){
        if(event.getInventory().getHolder() instanceof InventoryHandler handler) {
            handler.onOpen(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        if(event.getInventory().getHolder() instanceof InventoryHandler handler) {
            handler.onClose(event);
        }
    }

}
