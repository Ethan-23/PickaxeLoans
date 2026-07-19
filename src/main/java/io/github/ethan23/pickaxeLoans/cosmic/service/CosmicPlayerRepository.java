package io.github.ethan23.pickaxeLoans.cosmic.service;

import io.github.ethan23.pickaxeLoans.cosmic.model.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmicPlayerRepository {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public void addPlayer(UUID uuid){
        playerDataMap.put(uuid, new PlayerData());
    }

    public PlayerData getPlayer(UUID uuid){
        return  playerDataMap.get(uuid);
    }

}
