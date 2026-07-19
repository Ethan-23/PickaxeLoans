package io.github.ethan23.pickaxeLoans.task;

import io.github.ethan23.pickaxeLoans.service.LoanService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Repeating task that flushes accrued-tax changes to storage.
 *
 * <p>Tax accrual happens on every block break — far too often to write
 * through synchronously — so the service only marks those loans dirty and
 * this task batches them to disk. Dirty loans are also flushed once more on
 * shutdown. Lifecycle transitions (borrow, return, cancel, expire) are
 * persisted immediately and do not rely on this task.
 */
public class DirtyLoanCleaner {

    /** Run every 30 seconds (600 ticks). */
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
