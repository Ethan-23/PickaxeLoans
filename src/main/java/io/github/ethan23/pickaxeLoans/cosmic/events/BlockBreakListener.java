package io.github.ethan23.pickaxeLoans.cosmic.events;

import io.github.ethan23.pickaxeLoans.cosmic.model.CosmicOre;
import io.github.ethan23.pickaxeLoans.cosmic.model.OreType;
import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.item.PickaxeChecker;
import io.github.ethan23.pickaxeLoans.model.Loan;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    private final LoanService loanService;
    private final Set<CosmicOre> brokenOres;
    private final CosmicPlayerService cosmicPlayerService;

    public BlockBreakListener(LoanService loanService, CosmicPlayerService cosmicPlayerService) {
        this.loanService = loanService;
        this.cosmicPlayerService = cosmicPlayerService;
        this.brokenOres = new HashSet<>();
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(player.getGameMode() != GameMode.SURVIVAL){
            return;
        }

        UUID playerUUID = player.getUniqueId();

        Block block = event.getBlock();

        Material material = block.getType();
        event.setCancelled(true);

        if(!OreType.isOreType(material)) {
            return;
        }

        OreType oreType = OreType.valueOf(material.name());

        block.setType(Material.STONE);
        this.brokenOres.add(new CosmicOre(block.getLocation(), material));

        ItemStack pickaxe = player.getInventory().getItemInMainHand();



        handleCosmicGains(playerUUID, pickaxe, oreType);

    }

    private void handleCosmicGains(UUID playerUUID, ItemStack pickaxe, OreType oreType) {

        BigDecimal energyGain = oreType.getEnergyGain();
        BigDecimal experienceGain = oreType.getExperienceGain();

        if(loanService.isBorrower(playerUUID) && PickaxeChecker.isLoanItem(pickaxe)) {

            Loan loan = loanService.getBorrowersLoan(playerUUID);

            energyGain = loanService.accruedEnergy(loan, energyGain);
            experienceGain = loanService.accruedXp(loan, experienceGain);
        }

        cosmicPlayerService.addEnergy(playerUUID, energyGain);
        cosmicPlayerService.addExperience(playerUUID, experienceGain);


    }

    public void replaceBrokenOres(){
        for(CosmicOre cosmicOre : brokenOres){
            if(cosmicOre.location() == null || cosmicOre.material() == null){
                continue;
            }

            cosmicOre.location().getBlock().setType(cosmicOre.material());
        }
        brokenOres.clear();
    }

}
