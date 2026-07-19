package io.github.ethan23.pickaxeLoans.cosmic.model;

import org.bukkit.Material;

import java.math.BigDecimal;

public enum OreType {
    COAL_ORE(BigDecimal.valueOf(3), BigDecimal.valueOf(2)),
    IRON_ORE(BigDecimal.valueOf(6), BigDecimal.valueOf(7)),
    LAPIS_ORE(BigDecimal.valueOf(10), BigDecimal.valueOf(25)),
    REDSTONE_ORE(BigDecimal.valueOf(15), BigDecimal.valueOf(40)),
    GOLD_ORE(BigDecimal.valueOf(20), BigDecimal.valueOf(111)),
    DIAMOND_ORE(BigDecimal.valueOf(25), BigDecimal.valueOf(286)),
    EMERALD_ORE(BigDecimal.valueOf(40), BigDecimal.valueOf(512))
    ;

    private final BigDecimal energyGain;
    private final BigDecimal experienceGain;

    OreType(BigDecimal energyGain, BigDecimal experienceGain){
        this.energyGain = energyGain;
        this.experienceGain = experienceGain;
    }

    public BigDecimal getEnergyGain() {
        return energyGain;
    }

    public BigDecimal getExperienceGain() {
        return experienceGain;
    }

    public static boolean isOreType(Material material){
        try {
            OreType.valueOf(material.name());
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }
}
