package pl.tenfajnybartek.dropplugin.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.database.Database;
import pl.tenfajnybartek.dropplugin.managers.UserManager;

/**
 * Zadanie okresowego zapisu stanu graczy.
 */
public class SaverTask extends BukkitRunnable {
    private final DropPlugin plugin;
    private final UserManager userManager;
    private final Database database;

    public SaverTask(DropPlugin plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.database = plugin.getDatabase();
        // Uruchamiamy synchronously (bezpiecznie dla Bukkit API)
        this.runTaskTimer(plugin, 20L, 6000L);
    }

    @Override
    public void run() {
        try {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                this.database.sendEmptyUpdate();
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Ensure that saving player data is thread-safe
                    this.userManager.save(player.getUniqueId());
                }
            }
        } catch (Exception e) {
            // używamy loggera pluginu (mieliśmy błąd "plugin" niezdefiniowane)
            this.plugin.getLogger().warning("Błąd podczas zapisu użytkownika: " + e.getMessage());
            e.printStackTrace();
        }
    }
}