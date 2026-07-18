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

public abstract class InventoryGUI implements InventoryHandler {

    private final Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();

    public InventoryGUI(int size, Component title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void addButton(int slot, InventoryButton inventoryButton){
        this.buttonMap.put(slot, inventoryButton);
    }

    public void removeButton(int slot){
        this.buttonMap.remove(slot);
    }

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
