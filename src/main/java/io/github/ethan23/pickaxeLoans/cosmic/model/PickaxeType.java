package io.github.ethan23.pickaxeLoans.cosmic.model;

import org.bukkit.Material;

public enum PickaxeType {
    WOODEN_PICKAXE,
    STONE_PICKAXE,
    GOLDEN_PICKAXE,
    IRON_PICKAXE,
    DIAMOND_PICKAXE
    ;

    public static boolean isPickaxeType(Material material){
        try {
            PickaxeType.valueOf(material.name());
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }

}
