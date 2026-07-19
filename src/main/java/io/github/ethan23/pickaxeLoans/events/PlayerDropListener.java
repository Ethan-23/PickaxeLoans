package io.github.ethan23.pickaxeLoans.events;

import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import io.github.ethan23.pickaxeLoans.item.LoanKeys;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropListener implements Listener {

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainerView pdc = event.getItemDrop().getItemStack().getPersistentDataContainer();
        if(pdc.has(LoanKeys.loanKey)){
            player.sendMessage(ColorTextBuilder.parse("<red>You cannot drop a loaned item!"));
            event.setCancelled(true);
        }
    }

}
