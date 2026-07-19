package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.commands.LoanCommand;
import io.github.ethan23.pickaxeLoans.commands.LoanCommandTabComplete;
import io.github.ethan23.pickaxeLoans.cosmic.CosmicModule;
import io.github.ethan23.pickaxeLoans.events.InventoryMoveItemListener;
import io.github.ethan23.pickaxeLoans.task.DirtyLoanCleaner;
import io.github.ethan23.pickaxeLoans.database.LoanStorage;
import io.github.ethan23.pickaxeLoans.database.SqliteLoanStorage;
import io.github.ethan23.pickaxeLoans.events.PlayerDeathListener;
import io.github.ethan23.pickaxeLoans.events.PlayerDropListener;
import io.github.ethan23.pickaxeLoans.events.PlayerJoinListener;
import io.github.ethan23.pickaxeLoans.gui.GUIListener;
import io.github.ethan23.pickaxeLoans.gui.PlayerInputListener;
import io.github.ethan23.pickaxeLoans.service.LoanRepository;
import io.github.ethan23.pickaxeLoans.service.LoanService;
import io.github.ethan23.pickaxeLoans.task.LoanUpdateTick;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class PickaxeLoans extends JavaPlugin {

    private static PickaxeLoans instance;
    private LoanService loanService;
    private LoanUpdateTick loanUpdateTick;
    private DirtyLoanCleaner dirtyLoanCleaner;
    private LoanStorage storage;
    private CosmicModule cosmicModule;

    @Override
    public void onEnable() {
        instance = this;

        //Storage
        this.storage = new SqliteLoanStorage(getDataFolder().toPath().resolve("loans.db"));
        try {
            storage.init();
        }catch (Exception e){
            getLogger().log(Level.SEVERE, "Could not open loan database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LoanRepository loanRepository = new LoanRepository();
        this.loanService = new LoanService(loanRepository, this.storage, getLogger());
        this.loanService.loadFromStorage();

        //Cosmic Module
        this.cosmicModule = new CosmicModule();
        cosmicModule.enable(this, loanService);

        //Listeners
        PlayerInputListener playerInputListener = new PlayerInputListener();
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(playerInputListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this.loanService), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDropListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this.loanService), this);
        Bukkit.getPluginManager().registerEvents(new InventoryMoveItemListener(), this);

        //Commands
        Objects.requireNonNull(this.getCommand("loan")).setExecutor(new LoanCommand(this.loanService, playerInputListener, cosmicModule.getCosmicPlayerService()));
        Objects.requireNonNull(this.getCommand("loan")).setTabCompleter(new LoanCommandTabComplete());

        //Update
        this.loanUpdateTick = new LoanUpdateTick(this.loanService);
        this.loanUpdateTick.init(this);
        this.dirtyLoanCleaner = new DirtyLoanCleaner(this.loanService);
        this.dirtyLoanCleaner.init(this);

    }

    public static PickaxeLoans getPlugin(){
        return instance;
    }

    @Override
    public void onDisable() {

        if(cosmicModule != null){
            cosmicModule.disable();
        }

        if(this.loanUpdateTick != null){
            this.loanUpdateTick.cancel();
        }

        if(this.dirtyLoanCleaner != null){
            this.dirtyLoanCleaner.cancel();
        }

        if(this.loanService != null){
            this.loanService.flushDirtyLoans();
        }

        if(this.storage != null){
            this.storage.close();
        }
    }
}
