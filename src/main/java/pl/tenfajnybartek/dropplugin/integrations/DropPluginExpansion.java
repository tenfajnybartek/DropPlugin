package pl.tenfajnybartek.dropplugin.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.objects.User;

public class DropPluginExpansion extends PlaceholderExpansion {
    
    private final DropPlugin plugin;
    
    public DropPluginExpansion(DropPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "dropplugin";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "tenfajnybartek";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return null;
        }
        
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return null;
        }

        if (params.equalsIgnoreCase("level")) {
            User user = plugin.getUserManager().getUserIfLoaded(onlinePlayer.getUniqueId());
            if (user == null) {
                return "0";
            }
            return String.valueOf(user.getLvl());
        }

        if (params.equalsIgnoreCase("points")) {
            User user = plugin.getUserManager().getUserIfLoaded(onlinePlayer.getUniqueId());
            if (user == null) {
                return "0";
            }
            return String.valueOf(user.getPoints());
        }

        if (params.equalsIgnoreCase("points_required")) {
            User user = plugin.getUserManager().getUserIfLoaded(onlinePlayer.getUniqueId());
            if (user == null) {
                return "0";
            }
            return String.valueOf(user.getPointsRequired());
        }

        if (params.equalsIgnoreCase("points_to_next")) {
            User user = plugin.getUserManager().getUserIfLoaded(onlinePlayer.getUniqueId());
            if (user == null) {
                return "0";
            }
            return String.valueOf(user.getPointsToNextLevel());
        }
        
        return null;
    }
}
