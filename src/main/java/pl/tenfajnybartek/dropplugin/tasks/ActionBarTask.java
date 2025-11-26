package pl.tenfajnybartek.dropplugin.tasks;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.utils.DataUtils;
import pl.tenfajnybartek.dropplugin.utils.TitleUtils;


public class ActionBarTask extends BukkitRunnable {
    private final ConfigManager config;
    private final DropPlugin plugin;

    public ActionBarTask(DropPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        if (this.config.isActionbarStatus()) {
            // Run the task every 20 ticks (1 second) on the main thread
            this.runTaskTimer(plugin, 20L, 20L);
        }
    }

    @Override
    public void run() {
        // Cache config values to reduce redundant calls
        if (!this.config.isActionbarStatus()) return;
        boolean turboDrop = this.config.isTurboDrop();
        String actionbarMessage = this.config.getActionbarMessage();

        if (turboDrop && actionbarMessage != null && !actionbarMessage.isEmpty()) {
            String formattedMessage = StringUtils.replace(actionbarMessage, "{TIME}", DataUtils.durationToString(this.config.getTurboDrop()));
            for (Player player : Bukkit.getOnlinePlayers()) {
                TitleUtils.sendActionbar(player, formattedMessage);
            }
        }
    }

    public void cancelTask() {
        try {
            this.cancel();
        } catch (IllegalStateException ignored) {
        }
    }
}