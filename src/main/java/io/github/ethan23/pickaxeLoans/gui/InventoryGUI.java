package io.github.ethan23.pickaxeLoans.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for button-driven menus.
 *
 * <p>The GUI passes itself as the {@link org.bukkit.inventory.InventoryHolder}
 * when creating its inventory — that back-reference is how {@code GUIListener}
 * recognises plugin menus and routes events back to this instance. All clicks
 * are canceled by default; slots registered through {@link #addButton}
 * delegate to that button's handler.
 *
 * <p>Buttons are only painted into the inventory by {@link #decorate()},
 * which runs automatically on open. After changing buttons while the menu is
 * already open, call it again to refresh what the player sees.
 */
public abstract class InventoryGUI implements InventoryHandler {

    private final Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();

    public InventoryGUI(int size, Component title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    /** Registers (or replaces) the button for a slot. Takes effect on the next {@link #decorate()}. */
    public void addButton(int slot, InventoryButton inventoryButton){
        this.buttonMap.put(slot, inventoryButton);
    }

    /** Unregisters the button for a slot. The painted icon remains until repainted. */
    public void removeButton(int slot){
        this.buttonMap.remove(slot);
    }

    /** Paints every registered button's icon into its slot. */
    public void decorate() {
        this.buttonMap.forEach((slot, button) -> this.inventory.setItem(slot, button.getIcon()));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        InventoryButton button = this.buttonMap.get(slot);
        if(button != null){
            button.onClick(event);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.decorate();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {}

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
