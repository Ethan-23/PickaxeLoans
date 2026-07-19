package io.github.ethan23.pickaxeLoans.database;

import io.github.ethan23.pickaxeLoans.LoanService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DirtyLoanCleaner {

    private static final int TICK_UPDATE_AMOUNT = 20 * 30;

    private final LoanService loanService;
    private Integer taskID;

    public DirtyLoanCleaner(LoanService loanService) {
        this.loanService = loanService;
    }

    public void init(Plugin plugin){
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            loanService.flushDirtyLoans();
        }, 0, TICK_UPDATE_AMOUNT);
    }

    public void cancel(){
        if(this.taskID != null){
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }
}
