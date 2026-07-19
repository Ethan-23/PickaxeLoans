package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.commands.LoanCommand;
import io.github.ethan23.pickaxeLoans.commands.LoanCommandTabComplete;
import io.github.ethan23.pickaxeLoans.events.PlayerDeathListener;
import io.github.ethan23.pickaxeLoans.events.PlayerDropListener;
import io.github.ethan23.pickaxeLoans.events.PlayerJoinListener;
import io.github.ethan23.pickaxeLoans.gui.GUIListener;
import io.github.ethan23.pickaxeLoans.gui.PlayerInputListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PickaxeLoans extends JavaPlugin {

    private static PickaxeLoans instance;
    private LoanUpdateTick loanUpdateTick;

    @Override
    public void onEnable() {
        instance = this;
        LoanRepository loanRepository = new LoanRepository();
        LoanService loanService = new LoanService(loanRepository);

        //Listeners
        PlayerInputListener playerInputListener = new PlayerInputListener();
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(playerInputListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(loanService), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(loanService), this);

        //Commands
        Objects.requireNonNull(this.getCommand("loan")).setExecutor(new LoanCommand(loanService, playerInputListener));
        Objects.requireNonNull(this.getCommand("loan")).setTabCompleter(new LoanCommandTabComplete());

        //Update
        this.loanUpdateTick = new LoanUpdateTick(loanService);
        loanUpdateTick.startTickUpdate(this);

    }

    public static PickaxeLoans getPlugin(){
        return instance;
    }

    @Override
    public void onDisable() {
        loanUpdateTick.cancelRepeatingTask();
    }
}
