package io.github.ethan23.pickaxeLoans.cosmic.model;

import java.math.BigDecimal;

public class PlayerData {
    BigDecimal experience;
    BigDecimal energy;

    public PlayerData() {
        this.experience = BigDecimal.ZERO;
        this.energy = BigDecimal.ZERO;
    }

    public BigDecimal getExperience() {
        return experience;
    }

    public void setExperience(BigDecimal experience) {
        this.experience = experience;
    }

    public BigDecimal getEnergy() {
        return energy;
    }

    public void setEnergy(BigDecimal energy) {
        this.energy = energy;
    }

    public void increaseEnergy(BigDecimal amount){
        this.energy = this.energy.add(amount);
    }

    public void increaseExperience(BigDecimal amount){
        this.experience = this.experience.add(amount);
    }

    public void removeEnergy(BigDecimal amount) {
        this.energy = this.energy.subtract(amount);
    }
}
