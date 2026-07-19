package io.github.ethan23.pickaxeLoans.cosmic.task;

import io.github.ethan23.pickaxeLoans.cosmic.events.BlockBreakListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SimpleOreRespawn {

    private static final int TICK_UPDATE_AMOUNT = 100;
    private final BlockBreakListener blockBreakListener;
    private Integer taskID;

    public SimpleOreRespawn(BlockBreakListener blockBreakListener) {
        this.blockBreakListener = blockBreakListener;
    }

    public void init(Plugin plugin){
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            blockBreakListener.replaceBrokenOres();
        }, 0, TICK_UPDATE_AMOUNT);
    }

    public void cancel(){
        if(this.taskID != null){
            blockBreakListener.replaceBrokenOres();
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

}
