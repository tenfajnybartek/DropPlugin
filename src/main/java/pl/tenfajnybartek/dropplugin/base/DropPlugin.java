package pl.tenfajnybartek.dropplugin.base;

import org.bukkit.plugin.java.JavaPlugin;
import pl.tenfajnybartek.dropplugin.commands.ADropCommand;
import pl.tenfajnybartek.dropplugin.commands.DropCommand;
import pl.tenfajnybartek.dropplugin.commands.LevelCommand;
import pl.tenfajnybartek.dropplugin.configuration.Config;
import pl.tenfajnybartek.dropplugin.configuration.DropConfig;
import pl.tenfajnybartek.dropplugin.database.Database;
import pl.tenfajnybartek.dropplugin.listeners.*;
import pl.tenfajnybartek.dropplugin.managers.DropManager;
import pl.tenfajnybartek.dropplugin.managers.UserManager;
import pl.tenfajnybartek.dropplugin.tasks.SaverTask;
import pl.tenfajnybartek.dropplugin.utils.DropMenu;

import java.util.logging.Logger;

public class DropPlugin extends JavaPlugin {
    private final Logger logger = this.getLogger();

    private UserManager userManager;
    private DropManager dropManager;
    private Config config;
    private DropConfig dropConfig;
    private DropMenu dropMenu;
    private Database database;

    private boolean forceDisable = false;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        this.logger.info("Włączanie pluginu DropPlugin...");

        // 1. Konfigi
        this.config = new Config(this);
        this.dropConfig = new DropConfig(this);

        // 2. Managerowie
        this.userManager = new UserManager(this); // patch: przekazujesz plugin!
        this.dropManager = new DropManager(this);
        this.dropMenu = new DropMenu(this);

        // 3. Baza danych NA KOŃCU!
        this.database = new Database(this);

        registerListeners();
        registerCommands();
        new SaverTask(this);
        this.logger.info("Załadowano plugin DropPlugin w " + (double) (System.currentTimeMillis() - startTime) / 1000.0 + "s");
    }

    @Override
    public void onDisable() {
        if (!this.forceDisable) {
            if (userManager != null) {
                userManager.saveAllUsersSync();
            }
            if (this.database != null) {
                this.database.disconnect();
            }
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new UserLevelChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new UserPointsChangeListener(this), this);
    }

    private void registerCommands() {
        getCommand("drop").setExecutor(new DropCommand(this));
        getCommand("adrop").setExecutor(new ADropCommand(this));
        getCommand("level").setExecutor(new LevelCommand(this));
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public DropManager getDropManager() {
        return this.dropManager;
    }

    public DropConfig getDropConfig() {
        return this.dropConfig;
    }

    public DropMenu getDropMenu() {
        return this.dropMenu;
    }

    public Database getDatabase() {
        return this.database;
    }

    public Config getPluginConfig() {
        return this.config;
    }
}