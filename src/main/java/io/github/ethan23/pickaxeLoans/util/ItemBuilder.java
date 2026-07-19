package io.github.ethan23.pickaxeLoans.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemBuilder {
    private ItemBuilder() {}

    protected static final Pattern COLOR_TAG = Pattern.compile("<(#?[a-zA-Z0-9]+)>");
    private static final int MAX_LORE_CHAR = 30;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static ItemStack of(Material material, String name, String... loreString) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(MM.deserialize(name).decoration(TextDecoration.ITALIC, false));
            if (loreString.length > 0) {
                breakDownLore(meta, loreString);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        return item;
    }

    private static void breakDownLore(ItemMeta meta, String[] loreString){
        List<Component> lore = new ArrayList<>();

        for(String line : loreString){

            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();
            int lineCharCount = 0;
            String activeColor = "";

            for (String word : words) {

                int wordVisibleLength = PlainTextComponentSerializer.plainText()
                        .serialize(mm(word))
                        .length();

                if (lineCharCount > 0 && lineCharCount + 1 + wordVisibleLength > MAX_LORE_CHAR) {
                    lore.add(mm(currentLine.toString()));
                    currentLine.setLength(0);
                    lineCharCount = 0;

                    if (!activeColor.isEmpty()) {
                        currentLine.append(activeColor);
                    }
                }

                if (lineCharCount > 0) {
                    currentLine.append(" ");
                    lineCharCount += 1;
                }

                currentLine.append(word);
                Matcher matcher = COLOR_TAG.matcher(word);
                while (matcher.find()) {
                    activeColor = matcher.group();
                }
                lineCharCount += wordVisibleLength;
            }

            if (!currentLine.isEmpty()) {
                lore.add(mm(currentLine.toString()));
            }
        }

        meta.lore(lore);
    }

    protected static Component mm(String text) {
        return MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }
}
