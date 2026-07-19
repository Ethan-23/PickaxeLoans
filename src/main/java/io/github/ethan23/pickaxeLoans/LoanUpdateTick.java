package io.github.ethan23.pickaxeLoans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

public class LoanUpdateTick {

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
