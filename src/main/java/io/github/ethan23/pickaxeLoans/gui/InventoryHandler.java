package io.github.ethan23.pickaxeLoans.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public interface InventoryHandler extends InventoryHolder {

    void onClick(InventoryClickEvent event);

    void onOpen(InventoryOpenEvent event);

    void onClose(InventoryCloseEvent event);

}
