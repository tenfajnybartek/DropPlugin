package pl.tenfajnybartek.dropplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.dropplugin.database.Database;
import pl.tenfajnybartek.dropplugin.objects.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    public User getUser(Player player) {
        if (player == null) return null;
        UUID id = player.getUniqueId();

        User cached = this.userMap.get(id);
        if (cached != null) return cached;

        if (!Bukkit.isPrimaryThread()) {
            return null;
        }

        User created = new User(player);
        User prev = this.userMap.putIfAbsent(id, created);
        return prev != null ? prev : created;
    }

    public User getUserIfLoaded(UUID uuid) {
        if (uuid == null) return null;
        return this.userMap.get(uuid);
    }

    public User getUser(UUID uuid) {
        if (uuid == null) return null;
        User u = this.userMap.get(uuid);
        if (u != null) return u;
        if (Bukkit.isPrimaryThread()) {
            Player p = Bukkit.getPlayer(uuid);
            return p != null ? getUser(p) : null;
        }
        return null;
    }

    public void save(UUID uuid) {
        if (uuid == null) return;
        User user = this.userMap.get(uuid);
        if (user == null) {
            if (Bukkit.isPrimaryThread()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) user = getUser(p);
            }
        }
        if (user == null) return;

        try {
            Database db = Database.getInstance();
            if (db != null) db.saveUser(user);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Błąd podczas zapisu użytkownika " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeUserFromCache(UUID uuid) {
        if (uuid == null) return;
        this.userMap.remove(uuid);
    }

    public Map<UUID, User> getUserMap() {
        return this.userMap;
    }
}