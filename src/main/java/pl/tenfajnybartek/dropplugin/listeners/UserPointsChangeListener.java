package pl.tenfajnybartek.dropplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.events.UserPointsChangeEvent;
import pl.tenfajnybartek.dropplugin.objects.User;

public class UserPointsChangeListener implements Listener {
    private final ConfigManager config;

    public UserPointsChangeListener(DropPlugin plugin) {
        this.config = plugin.getPluginConfig();
    }

    @EventHandler
    public void onPktChange(UserPointsChangeEvent event) {
        User u = event.getUser();
        if (u == null) return;
        if (u.getLvl() >= this.config.getMaxLevel()) {
            return;
        }

        int levelPoints = event.getLevelPoints();

        if (levelPoints >= u.getPointsRequired()) {
            int newLevel = u.getLvl() + 1;
            u.setLvl(newLevel);
            u.setPoints(0);
        }
    }
}