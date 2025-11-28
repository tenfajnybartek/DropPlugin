package pl.tenfajnybartek.dropplugin.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.UserManager;
import pl.tenfajnybartek.dropplugin.objects.User;

public class AsyncPlayerChatListener implements Listener {
    private final UserManager userManager;

    public AsyncPlayerChatListener(DropPlugin plugin) {
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        User user = this.userManager.getUserIfLoaded(event.getPlayer().getUniqueId());
        Component message = event.message();
        if (user == null || message == null) {
            return;
        }

        Component prefix = Component.text("[" + user.getLvl() + "] ");
        Component newMessage = prefix.append(message);
        event.message(newMessage);
    }
}