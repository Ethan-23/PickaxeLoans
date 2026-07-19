package io.github.ethan23.pickaxeLoans.cosmic.service;

import io.github.ethan23.pickaxeLoans.cosmic.model.PlayerData;

import java.math.BigDecimal;
import java.util.UUID;

public class CosmicPlayerService {

    private final CosmicPlayerRepository cosmicPlayerRepository;

    public CosmicPlayerService(CosmicPlayerRepository cosmicPlayerRepository) {
        this.cosmicPlayerRepository = cosmicPlayerRepository;
    }

    public void addPlayer(UUID uuid){
        if(cosmicPlayerRepository.getPlayer(uuid) == null){
            this.cosmicPlayerRepository.addPlayer(uuid);
        }
    }

    public void addEnergy(UUID uuid, BigDecimal amount){
        this.cosmicPlayerRepository.getPlayer(uuid).increaseEnergy(amount);
    }

    public void addExperience(UUID uuid, BigDecimal amount){
        this.cosmicPlayerRepository.getPlayer(uuid).increaseExperience(amount);
    }

    public BigDecimal getExperience(UUID uuid){
        PlayerData playerData = this.cosmicPlayerRepository.getPlayer(uuid);
        return playerData == null ? BigDecimal.ZERO : playerData.getExperience();
    }

    public BigDecimal getEnergy(UUID uuid){
        PlayerData playerData = this.cosmicPlayerRepository.getPlayer(uuid);
        return playerData == null ? BigDecimal.ZERO : playerData.getEnergy();
    }
}
