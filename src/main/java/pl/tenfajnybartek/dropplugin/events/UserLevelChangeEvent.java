package pl.tenfajnybartek.dropplugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.tenfajnybartek.dropplugin.objects.User;

public class UserLevelChangeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final int level;
    private final Player player;
    private final User user;

    public UserLevelChangeEvent(User user) {
        this.user = user;
        this.level = user.getLvl();
        this.player = user.getPlayer();
    }

    public int getLevel() {
        return this.level;
    }

    public Player getPlayer() {
        return this.player;
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}