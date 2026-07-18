package io.github.ethan23.pickaxeLoans.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class InventoryButton {
    private final ItemStack icon;
    private final Consumer<InventoryClickEvent> handler;

    public InventoryButton(ItemStack icon, Consumer<InventoryClickEvent> handler) {
        this.icon = icon;
        this.handler = handler;
    }


    public InventoryButton(ItemStack icon) {
        this.icon = icon;
        this.handler = e -> {};
    }

    public ItemStack getIcon() { return icon; }
    public void onClick(InventoryClickEvent event) { handler.accept(event); }
}