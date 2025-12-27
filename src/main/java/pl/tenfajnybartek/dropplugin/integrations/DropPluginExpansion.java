package pl.tenfajnybartek.dropplugin.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.database.Database;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.objects.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DropPluginExpansion extends PlaceholderExpansion {
    
    private final DropPlugin plugin;
    
    // Cache for top level players to avoid frequent database queries
    // Key: rank position, Value: CacheEntry with player name and timestamp
    private final Map<Integer, CacheEntry> topLevelCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 30000; // 30 seconds cache
    
    private static class CacheEntry {
        final String playerName;
        final long timestamp;
        
        CacheEntry(String playerName) {
            this.playerName = playerName;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
    
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
        // Handle toplevel-x placeholder (doesn't require player context)
        if (params.toLowerCase().startsWith("toplevel-")) {
            String rankStr = params.substring("toplevel-".length());
            try {
                int rank = Integer.parseInt(rankStr);
                if (rank < 1) {
                    return ConfigManager.getConfigManager().getToplevelNoPlayer();
                }
                
                // Check cache first
                CacheEntry cached = topLevelCache.get(rank);
                if (cached != null && !cached.isExpired()) {
                    return cached.playerName != null ? cached.playerName : ConfigManager.getConfigManager().getToplevelNoPlayer();
                }
                
                Database db = Database.getInstance();
                if (db == null) {
                    return ConfigManager.getConfigManager().getToplevelNoPlayer();
                }
                String playerName = db.getTopLevelPlayer(rank);
                
                // Update cache
                topLevelCache.put(rank, new CacheEntry(playerName));
                
                if (playerName == null) {
                    return ConfigManager.getConfigManager().getToplevelNoPlayer();
                }
                return playerName;
            } catch (NumberFormatException e) {
                return ConfigManager.getConfigManager().getToplevelNoPlayer();
            }
        }
        
        // Player-specific placeholders require online player
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
