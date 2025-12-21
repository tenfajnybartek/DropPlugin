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

public class User {
    private HashMap<String, Integer> minedDrops;
    private final Set<String> disabledDrops;
    private boolean cobble;
    private boolean messages;
    private final UUID identifier;
    private long turboDrop;
    private long turboExp;
    private int points;
    private int lvl;

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
    }

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
    }

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
        int pointsToLvl = ConfigManager.getConfigManager().getPointsToLvlUp();
        if (pointsToLvl <= 0) {
            return 100; // domyślna wartość bezpieczna
        }
        return this.lvl * pointsToLvl;
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

}