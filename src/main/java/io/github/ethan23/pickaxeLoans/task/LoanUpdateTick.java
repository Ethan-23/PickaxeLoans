package io.github.ethan23.pickaxeLoans.task;

import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

/**
 * Repeating task that drives time-based loan transitions: listing expiry
 * and loan-end returns.
 *
 * <p>Both checks are heap-driven — the service only inspects the top of the
 * listing-expiration and loan-end heaps, so a tick where nothing is due
 * costs O(1) instead of a scan over every loan. Borrowers whose loans just
 * ended have their inventories reconciled to remove the returned pickaxe.
 */
public class LoanUpdateTick {

    /** Run every second (20 ticks). */
    private static final int TICK_UPDATE_AMOUNT = 20;

    private Integer taskID;
    private final LoanService loanService;

    public LoanUpdateTick(LoanService loanService) {
        this.loanService = loanService;
    }

    public void init(Plugin plugin){
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            loanService.checkExpirationHeap();
            handleEndsAtInventories(loanService.checkEndsAtHeap());
        }, 0, TICK_UPDATE_AMOUNT);
    }

    public void cancel(){
        if(taskID != null){
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    private void handleEndsAtInventories(Set<UUID> removedBorrowers){
        for(UUID uuid : removedBorrowers){
            PickaxeChecker.removeLoan(loanService, uuid);
        }
    }
}
