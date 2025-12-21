package pl.tenfajnybartek.dropplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.utils.ChatUtils;
import pl.tenfajnybartek.dropplugin.events.UserLevelChangeEvent;

public class UserLevelChangeListener implements Listener {
    private final ConfigManager config;

    public UserLevelChangeListener(DropPlugin plugin) {
        this.config = plugin.getPluginConfig();
    }

    @EventHandler
    public void userLevelChange(UserLevelChangeEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        int lvl = event.getLevel();

        if (lvl == this.config.getMaxLevel()) {
            String msgAll = this.config.getMaxLevelMessage()
                    .replace("{PLAYER}", player.getName())
                    .replace("{LVL}", String.valueOf(lvl));
            Bukkit.getOnlinePlayers().forEach(all -> ChatUtils.sendMessage(all, msgAll));
        } else if (this.config.getChatLevels().contains(lvl)) {
            String msgAll = this.config.getLevelMessage()
                    .replace("{PLAYER}", player.getName())
                    .replace("{LVL}", String.valueOf(lvl));
            Bukkit.getOnlinePlayers().forEach(all -> ChatUtils.sendMessage(all, msgAll));
        }

        String personal = this.config.getLvlUpMessage()
                .replace("{LVL}", String.valueOf(lvl))
                .replace("{POINTS}", String.valueOf(lvl * this.config.getPointsToLvlUp()));
        ChatUtils.sendMessage(player, personal);

        if (player.getLocation() != null && player.getWorld() != null) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
        }
    }
}