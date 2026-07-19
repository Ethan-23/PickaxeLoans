package io.github.ethan23.pickaxeLoans.cosmic;

import io.github.ethan23.pickaxeLoans.cosmic.events.BlockBreakListener;
import io.github.ethan23.pickaxeLoans.cosmic.events.PlayerJoinListener;
import io.github.ethan23.pickaxeLoans.cosmic.scoreboard.CosmicScoreboard;
import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerRepository;
import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.cosmic.task.ScoreboardUpdater;
import io.github.ethan23.pickaxeLoans.cosmic.task.SimpleOreRespawn;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CosmicModule {

    private CosmicPlayerRepository cosmicPlayerRegistry;
    private CosmicPlayerService cosmicPlayerService;

    private BlockBreakListener blockBreakListener;
    private SimpleOreRespawn simpleOreRespawn;
    private ScoreboardUpdater scoreboardUpdater;

    public void enable(Plugin plugin, LoanService loanService){
        this.cosmicPlayerRegistry = new CosmicPlayerRepository();

        this.cosmicPlayerService = new CosmicPlayerService(cosmicPlayerRegistry);

        this.blockBreakListener = new BlockBreakListener(loanService, this.cosmicPlayerService);
        Bukkit.getPluginManager().registerEvents(this.blockBreakListener, plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(cosmicPlayerService), plugin);

        this.simpleOreRespawn = new SimpleOreRespawn(this.blockBreakListener);
        this.simpleOreRespawn.init(plugin);

        this.scoreboardUpdater = new ScoreboardUpdater(new CosmicScoreboard(this.cosmicPlayerService));
        this.scoreboardUpdater.init(plugin);
    }

    public void disable(){
        if(this.simpleOreRespawn != null) {
            this.simpleOreRespawn.cancel();
        }

        if(this.scoreboardUpdater != null) {
            this.scoreboardUpdater.cancel();
        }
    }

    public CosmicPlayerService getCosmicPlayerService() {
        return this.cosmicPlayerService;
    }
}
