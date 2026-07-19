package io.github.ethan23.pickaxeLoans.cosmic.task;

import io.github.ethan23.pickaxeLoans.cosmic.scoreboard.CosmicScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ScoreboardUpdater {

    private static final int UPDATE_INTERVAL_TICKS = 100;

    private final CosmicScoreboard cosmicScoreboard;
    private Integer taskID;

    public ScoreboardUpdater(CosmicScoreboard cosmicScoreboard) {
        this.cosmicScoreboard = cosmicScoreboard;
    }

    public void init(Plugin plugin) {
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            cosmicScoreboard.updateAll();
        }, 0, UPDATE_INTERVAL_TICKS);
    }

    public void cancel() {
        if (this.taskID != null) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }
}
