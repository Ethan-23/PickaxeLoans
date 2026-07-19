package io.github.ethan23.pickaxeLoans.cosmic.scoreboard;

import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import io.github.ethan23.pickaxeLoans.util.NumberConversions;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

public class CosmicScoreboard {

    private static final String OBJECTIVE_NAME = "cosmic";
    private static final String EXPERIENCE_ENTRY = "experience";
    private static final String ENERGY_ENTRY = "energy";

    private final CosmicPlayerService cosmicPlayerService;

    public CosmicScoreboard(CosmicPlayerService cosmicPlayerService) {
        this.cosmicPlayerService = cosmicPlayerService;
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    private void update(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);

        if (objective == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY,
                    ColorTextBuilder.parse("<gold><bold>Cosmic"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.numberFormat(NumberFormat.blank());
            player.setScoreboard(board);
        }

        UUID uuid = player.getUniqueId();

        Score experienceScore = objective.getScore(EXPERIENCE_ENTRY);
        experienceScore.setScore(2);
        experienceScore.customName(ColorTextBuilder.parse("<yellow>Experience: <white>"
                + NumberConversions.formattedNumberDisplay(cosmicPlayerService.getExperience(uuid))));

        Score energyScore = objective.getScore(ENERGY_ENTRY);
        energyScore.setScore(1);
        energyScore.customName(ColorTextBuilder.parse("<aqua>Energy: <white>"
                + NumberConversions.formattedNumberDisplay(cosmicPlayerService.getEnergy(uuid))));
    }
}
