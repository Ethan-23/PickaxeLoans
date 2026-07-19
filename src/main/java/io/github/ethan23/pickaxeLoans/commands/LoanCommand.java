package io.github.ethan23.pickaxeLoans.commands;

import io.github.ethan23.pickaxeLoans.cosmic.service.CosmicPlayerService;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.gui.PlayerInputListener;
import io.github.ethan23.pickaxeLoans.gui.menu.LoanCreateMenu;
import io.github.ethan23.pickaxeLoans.gui.menu.LoanListingMenu;
import io.github.ethan23.pickaxeLoans.util.ColorTextBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.ethan23.pickaxeLoans.item.PickaxeChecker.checkLoanCreateRequirements;

public class LoanCommand implements CommandExecutor {

    private final LoanService loanService;
    private final PlayerInputListener playerInputListener;
    private final CosmicPlayerService cosmicPlayerService;

    public LoanCommand(LoanService loanService, PlayerInputListener playerInputListener, CosmicPlayerService cosmicPlayerService) {
        this.playerInputListener = playerInputListener;
        this.cosmicPlayerService = cosmicPlayerService;
        this.loanService = loanService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if(!(sender instanceof Player player)){
            return true;
        }

        if(args.length == 0){
            player.sendMessage(ColorTextBuilder.parse("<yellow>Opening Loan Listings"));
            player.openInventory(new LoanListingMenu(player, loanService, playerInputListener, cosmicPlayerService).getInventory());
            return true;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("create")){

                ItemStack heldItem = player.getInventory().getItemInMainHand().clone();
                if(!checkLoanCreateRequirements(heldItem)){
                    player.sendMessage(ColorTextBuilder.parse("<red>You cannot loan this item!"));
                    return true;
                }

                player.sendMessage(ColorTextBuilder.parse("<yellow>Creating Loan Listings"));
                player.openInventory(new LoanCreateMenu(playerInputListener, loanService, player, heldItem, player.getInventory().getHeldItemSlot()).getInventory());

                return true;
            }
        }



        return true;
    }


}
