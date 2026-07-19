package io.github.ethan23.pickaxeLoans.events;

import io.github.ethan23.pickaxeLoans.LoanService;
import io.github.ethan23.pickaxeLoans.PickaxeChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final LoanService loanService;

    public PlayerJoinListener(LoanService loanService) {
        this.loanService = loanService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PickaxeChecker.removeLoan(player.getInventory(), loanService, uuid);

    }

}
