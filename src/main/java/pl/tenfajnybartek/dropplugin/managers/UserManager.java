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
        if (player == null) {
            Bukkit.getLogger().warning("Attempted to get User for null player");
            return null;
        }
        
        UUID id = player.getUniqueId();
        User cached = this.userMap.get(id);
        if (cached != null) return cached;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getLogger().warning("Attempted to create User for " + player.getName() + " from async thread");
            return null;
        }

        try {
            User created = new User(player);
            User prev = this.userMap.putIfAbsent(id, created);
            return prev != null ? prev : created;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to create User for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Zwraca User tylko jeżeli jest już załadowany w pamięci (cache).
     * BEZPIECZNE do wywołania z wątków asynchronicznych.
     */
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

    /**
     * Zapisuje użytkownika domyślnie asynchronicznie (tak jak było).
     */
    public void save(UUID uuid) {
        save(uuid, false); // domyślnie async
    }

    /**
     * Zapisuje użytkownika synchronicznie lub asynchronicznie, w zależności od parametru sync.
     * Używaj sync = true podczas wyłączania pluginu!
     */
    public void save(UUID uuid, boolean sync) {
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
            if (db != null) db.saveUser(user, sync); // przekazujemy tryb dalej
        } catch (Exception e) {
            Bukkit.getLogger().warning("Błąd podczas zapisu użytkownika " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeUserFromCache(UUID uuid) {
        if (uuid == null) return;
        this.userMap.remove(uuid);
    }

    /**
     * Ładuje użytkownika do cache.
     * Używane przy wczytywaniu danych z bazy danych.
     * 
     * @param user Użytkownik do załadowania
     * @return true jeśli użytkownik został załadowany, false jeśli już istniał
     */
    public boolean loadUser(User user) {
        if (user == null || user.getIdentifier() == null) {
            return false;
        }
        User existing = this.userMap.putIfAbsent(user.getIdentifier(), user);
        return existing == null; // true jeśli był nowy, false jeśli już istniał
    }

    /**
     * Zwraca mapę użytkowników.
     * UWAGA: Używaj tej metody tylko do odczytu (np. liczenia użytkowników).
     * Do modyfikacji używaj dedykowanych metod jak loadUser().
     * 
     * @return Niemodyfikowalna mapa użytkowników
     */
    public Map<UUID, User> getUserMap() {
        return this.userMap;
    }
}