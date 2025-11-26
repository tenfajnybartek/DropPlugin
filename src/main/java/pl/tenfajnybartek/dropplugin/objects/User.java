package pl.tenfajnybartek.dropplugin.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.dropplugin.events.UserLevelChangeEvent;
import pl.tenfajnybartek.dropplugin.events.UserPointsChangeEvent;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.utils.MapUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Zaktualizowana klasa User:
 * - usunięto zależności od Guavy (Maps/Sets) na rzecz standardowych kolekcji
 * - odwołania do konfiguracji używają ConfigManager.getConfigManager()
 * - ResultSet constructor jest null-safe dla pól lastMessage/lastSender
 */
public class User {
    private HashMap<String, Integer> minedDrops;
    private final Set<String> disabledDrops;
    private boolean cobble;
    private boolean messages;
    private boolean msgToggle;
    private final UUID identifier;
    private long turboDrop;
    private long turboExp;
    private int points;
    private int lvl;

    // Nowe pola
    private String lastMessage;
    private UUID lastSender;

    // Konstruktor
    public User(Player player) {
        this.identifier = player.getUniqueId();
        this.cobble = true;
        this.messages = true;
        this.disabledDrops = new HashSet<>();
        this.minedDrops = new HashMap<>();
        this.turboDrop = 0L;
        this.turboExp = 0L;
        this.lvl = 1;
        this.points = 0;
        this.lastMessage = null;
        this.lastSender = null;
        this.msgToggle = false;
    }

    // Konstruktor z ResultSet
    public User(ResultSet resultSet) throws SQLException {
        this.identifier = UUID.fromString(resultSet.getString("identifier"));
        this.cobble = resultSet.getBoolean("cobble");
        this.messages = resultSet.getBoolean("messages");
        this.turboDrop = resultSet.getLong("turboDrop");
        this.turboExp = resultSet.getLong("turboExp");
        this.lvl = resultSet.getInt("lvl");
        this.points = resultSet.getInt("points");
        this.minedDrops = MapUtils.deserializeMap(resultSet.getString("minedDrops"));
        this.disabledDrops = MapUtils.deserializeList(resultSet.getString("disabledDrops"));
        this.lastMessage = resultSet.getString("lastMessage"); // może być null

        String lastSenderStr = resultSet.getString("lastSender");
        if (lastSenderStr != null && !lastSenderStr.isEmpty()) {
            try {
                this.lastSender = UUID.fromString(lastSenderStr);
            } catch (IllegalArgumentException iae) {
                // niepoprawny UUID — ustawiamy null i logujemy ostrzeżenie
                this.lastSender = null;
                Bukkit.getLogger().warning("Niepoprawny lastSender UUID w DB dla identyfikatora " + this.identifier + ": " + lastSenderStr);
            }
        } else {
            this.lastSender = null;
        }

        // Jeśli kolumna msgToggle nie istnieje w starszej wersji bazy, domyślnie false
        try {
            this.msgToggle = resultSet.getBoolean("msgToggle");
        } catch (SQLException ignored) {
            this.msgToggle = false;
        }
    }

    // Gettery i settery
    public UUID getIdentifier() {
        return this.identifier;
    }

    public boolean isCobble() {
        return this.cobble;
    }

    public boolean isMessages() {
        return this.messages;
    }

    public void setMessages(boolean messages) {
        this.messages = messages;
    }

    public void setCobble(boolean cobble) {
        this.cobble = cobble;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
        Bukkit.getPluginManager().callEvent(new UserLevelChangeEvent(this));
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLvl() {
        return this.lvl;
    }

    public int getPointsRequired() {
        // używamy ConfigManager (nowa nazwa)
        return this.lvl * ConfigManager.getConfigManager().getPointsToLvlUp();
    }

    public int getPoints() {
        return this.points;
    }

    public void addPoints(int points) {
        if (ConfigManager.getConfigManager().isLvlStatus()) {
            this.points += points;
            Bukkit.getPluginManager().callEvent(new UserPointsChangeEvent(this));
        }
    }

    public int getPointsToNextLevel() {
        return this.getPointsRequired() - this.points;
    }

    public void addDrop(Drop drop, int amount) {
        if (drop == null) return;
        int drops = this.getDrop(drop.getName());
        this.minedDrops.put(drop.getName(), drops + amount);
    }

    public HashMap<String, Integer> getMinedDrops() {
        if (this.minedDrops == null) this.minedDrops = new HashMap<>();
        return this.minedDrops;
    }

    public int getDrop(String drop) {
        if (drop == null) {
            return 0;
        }
        if (this.minedDrops == null || this.minedDrops.isEmpty()) {
            return 0;
        }
        Integer val = this.minedDrops.get(drop);
        return val == null ? 0 : val;
    }

    public long getTurboExp() {
        return this.turboExp;
    }

    public void addTurboExp(long turboExp) {
        this.turboExp = turboExp;
    }

    public long getTurboDrop() {
        return this.turboDrop;
    }

    public void addTurboDrop(long turboDrop) {
        this.turboDrop = turboDrop;
    }

    public boolean isTurboDrop() {
        return this.getTurboDrop() > System.currentTimeMillis();
    }

    public boolean isTurboExp() {
        return this.getTurboExp() > System.currentTimeMillis();
    }

    public Set<String> getDisabledDrops() {
        return this.disabledDrops;
    }

    public boolean isDisabled(Drop drop) {
        return drop != null && this.disabledDrops.contains(drop.getName());
    }

    public void addDisabledDrop(Drop drop) {
        if (drop == null) return;
        this.disabledDrops.add(drop.getName());
    }

    public void removeDisabledDrop(Drop drop) {
        if (drop == null) return;
        this.disabledDrops.remove(drop.getName());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.identifier);
    }

    // Nowe metody dla wiadomości
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public UUID getLastSender() {
        return lastSender;
    }

    public void setLastSender(UUID lastSender) {
        this.lastSender = lastSender;
    }

    public boolean isMsgToggle() {
        return msgToggle;
    }

    public void setMsgToggle(boolean msgToggle) {
        this.msgToggle = msgToggle;
    }
}