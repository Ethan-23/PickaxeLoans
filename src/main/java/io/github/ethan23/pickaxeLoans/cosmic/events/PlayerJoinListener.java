package io.github.ethan23.pickaxeLoans.cosmic.events;

import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CosmicPlayerService cosmicPlayerService;

    public PlayerJoinListener(CosmicPlayerService cosmicPlayerService) {
        this.cosmicPlayerService = cosmicPlayerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        cosmicPlayerService.addPlayer(event.getPlayer().getUniqueId());
    }

}
