package pl.tenfajnybartek.dropplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.DropManager;


public class BlockBreakListener implements Listener {

    private final DropManager dropManager;

    public BlockBreakListener(DropPlugin plugin) {
        this.dropManager = plugin.getDropManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        try {
            this.dropManager.breakBlock(event);
        } catch (Exception e) {
            event.getPlayer().sendMessage("§cWystąpił błąd podczas przetwarzania dropu. Skontaktuj się z administratorem.");
            e.printStackTrace();
        }
    }
}