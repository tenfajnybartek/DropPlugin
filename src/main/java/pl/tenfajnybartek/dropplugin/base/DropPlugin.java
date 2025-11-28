package pl.tenfajnybartek.dropplugin.base;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tenfajnybartek.dropplugin.commands.ADropCommand;
import pl.tenfajnybartek.dropplugin.commands.DropCommand;
import pl.tenfajnybartek.dropplugin.commands.LevelCommand;
import pl.tenfajnybartek.dropplugin.database.Database;
import pl.tenfajnybartek.dropplugin.listeners.*;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.managers.DropConfigManager;
import pl.tenfajnybartek.dropplugin.managers.DropManager;
import pl.tenfajnybartek.dropplugin.managers.UserManager;
import pl.tenfajnybartek.dropplugin.tasks.ActionBarTask;
import pl.tenfajnybartek.dropplugin.tasks.SaverTask;
import pl.tenfajnybartek.dropplugin.utils.DropMenu;

import java.util.logging.Logger;

public class DropPlugin extends JavaPlugin {
    private Logger logger;

    private UserManager userManager;
    private DropManager dropManager;
    private Database database;
    private ConfigManager configManager;
    private DropConfigManager dropConfig;
    private SaverTask saverTask;
    private ActionBarTask actionBarTask;
    private DropMenu dropMenu;
    private boolean forceDisable = false;

    @Override
    public void onEnable() {
        this.logger = this.getLogger();

        try {
            long startTime = System.currentTimeMillis();
            this.logger.info("Initializacja pluginu tfbDrop...");
            this.configManager = new ConfigManager(this);
            this.dropConfig = new DropConfigManager(this);
            this.userManager = new UserManager();
            this.database = new Database(this);
            this.dropManager = new DropManager(this);
            this.dropMenu = new DropMenu(this);
            this.saverTask = new SaverTask(this);
            this.actionBarTask = new ActionBarTask(this);
            registerListeners();
            registerCommands();

            this.logger.info("Zaladowano plugin tfbDrop w " + (double)(System.currentTimeMillis() - startTime) / 1000.0 + "s");
        } catch (Throwable t) {
            this.forceDisable = true;
            logger.severe("Błąd podczas inicjalizacji pluginu tfbDrop: " + t.getMessage());
            t.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (!this.forceDisable) {
            if (this.userManager != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        this.userManager.save(player.getUniqueId());
                    } catch (Exception e) {
                        logger.warning("Błąd zapisu użytkownika " + player.getUniqueId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            if (this.saverTask != null) {
                this.saverTask.cancel();
            }
            if (this.actionBarTask != null) {
                this.actionBarTask.cancel();
                this.actionBarTask = null;
            }
            if (this.database != null) {
                try {
                    this.database.disconnect();
                } catch (Exception e) {
                    logger.warning("Błąd podczas rozłączania bazy danych: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("Shutdown przerwany z powodu wcześniejszej błędnej inicjalizacji (forceDisable=true).");
        }
    }

    private void registerListeners() {
        Object[] listeners = new Object[] {
                new AsyncPlayerChatListener(this),
                new BlockBreakListener(this),
                new InventoryClickListener(this),
                new PlayerJoinLeaveListener(this),
                new UserLevelChangeListener(this),
                new UserPointsChangeListener(this)
        };

        for (Object obj : listeners) {
            if (obj instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) obj, this);
            }
        }
    }

    private void registerCommands() {
        new DropCommand(this);
        new ADropCommand(this);
        new LevelCommand(this);
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public DropManager getDropManager() {
        return this.dropManager;
    }

    public DropConfigManager getDropConfig() {
        return this.dropConfig;
    }

    public DropMenu getDropMenu() {
        return this.dropMenu;
    }

    public Database getDatabase() {
        return this.database;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }
    public void reloadConfigManager() {
        this.configManager = new ConfigManager(this);

        if (this.actionBarTask != null) {
            this.actionBarTask.cancel();
            this.actionBarTask = null;
        }
        this.actionBarTask = new ActionBarTask(this);
    }
    public ConfigManager getPluginConfig() {
        return this.configManager;
    }
}