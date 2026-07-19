package io.github.ethan23.pickaxeLoans.events;

import io.github.ethan23.pickaxeLoans.item.LoanKeys;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final LoanService loanService;

    public PlayerDeathListener(LoanService loanService) {
        this.loanService = loanService;
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Iterator<ItemStack> iterator = event.getDrops().iterator();

        if(!loanService.isBorrower(uuid)) {
            return;
        }

        while(iterator.hasNext()){
            ItemStack itemStack = iterator.next();

            if(itemStack == null){
                continue;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();

            if(itemMeta == null){
                continue;
            }

            PersistentDataContainerView pdc = itemMeta.getPersistentDataContainer();
            if (!pdc.has(LoanKeys.loanKey)) {
                continue;
            }

            String loanUUIDString = pdc.get(LoanKeys.loanKey, PersistentDataType.STRING);
            if (loanUUIDString == null) {
                continue;
            }

            iterator.remove();
            loanService.returnLoan(UUID.fromString(loanUUIDString));
            player.sendMessage(ColorTextBuilder.parse("<yellow>Your loan has been returned!"));

        }


    }

}
