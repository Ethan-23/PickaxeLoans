package io.github.ethan23.pickaxeLoans.util;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemBuilder {
    private ItemBuilder() {}

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static ItemStack of(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(MM.deserialize(name).decoration(TextDecoration.ITALIC, false));
            if (lore.length > 0) {
                meta.lore(Arrays.stream(lore)
                        .map(l -> MM.deserialize(l).decoration(TextDecoration.ITALIC, false))
                        .toList());
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        return item;
    }
}
