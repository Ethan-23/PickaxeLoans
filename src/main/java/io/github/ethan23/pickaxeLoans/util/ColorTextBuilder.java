package io.github.ethan23.pickaxeLoans.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ColorTextBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Allows you to build a component from a string with miniMessage.
     * @param mm string to convert
     * @return Component of string
     */
    public static Component parse(String mm) {
        return MM.deserialize(mm).decoration(TextDecoration.ITALIC, false);
    }

}
