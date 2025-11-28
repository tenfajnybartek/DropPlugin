package pl.tenfajnybartek.dropplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.UserManager;

public class PlayerJoinLeaveListener implements Listener {
    private final UserManager userManager;

    public PlayerJoinLeaveListener(DropPlugin plugin) {
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        userManager.getUser(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        saveAndCleanup(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        saveAndCleanup(event.getPlayer());
    }

    private void saveAndCleanup(Player player) {
        if (player == null) return;
        userManager.save(player.getUniqueId());
        userManager.removeUserFromCache(player.getUniqueId());
    }
}
