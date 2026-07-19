package io.github.ethan23.pickaxeLoans.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * An inventory that handles its own events.
 *
 * <p>Extends {@link InventoryHolder} so implementations can register
 * themselves as the holder of the inventory they create. {@code GUIListener}
 * uses that back-reference to recognize plugin menus and route click, open,
 * and close events to the right menu — no registry of open GUIs is needed.
 */
public interface InventoryHandler extends InventoryHolder {

    /** Handles a click anywhere in this inventory's view. */
    void onClick(InventoryClickEvent event);

    /** Called when a player opens this inventory. */
    void onOpen(InventoryOpenEvent event);

    /** Called when a player closes this inventory. */
    void onClose(InventoryCloseEvent event);

}
