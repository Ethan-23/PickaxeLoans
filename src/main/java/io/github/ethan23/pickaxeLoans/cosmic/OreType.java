package io.github.ethan23.pickaxeLoans.cosmic;

import org.bukkit.Material;

public enum OreType {
    COAL_ORE,
    IRON_ORE,
    LAPIS_ORE,
    REDSTONE_ORE,
    GOLD_ORE,
    DIAMOND_ORE,
    EMERALD_ORE
    ;

    public static boolean isOreType(Material material){
        try {
            OreType.valueOf(material.name());
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }
}
