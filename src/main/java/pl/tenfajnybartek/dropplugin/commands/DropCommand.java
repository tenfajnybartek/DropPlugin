package pl.tenfajnybartek.dropplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.utils.ChatUtils;
import pl.tenfajnybartek.dropplugin.utils.DropMenu;

public class DropCommand implements CommandExecutor {
    private final DropMenu dropMenu;

    public DropCommand(DropPlugin plugin) {
        this.dropMenu = plugin.getDropMenu();
        if (plugin.getCommand("drop") != null) {
            plugin.getCommand("drop").setExecutor(this);
        } else {
            plugin.getLogger().warning("Command 'drop' is not defined in plugin.yml");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // permission check
        if (!sender.hasPermission("dropplugin.cmd.drop")) {
            ChatUtils.sendMessage(sender, "&4Blad: &cNie masz uprawnien do tej komendy! &7(tfbhc.cmd.drop)");
            return true;
        }

        // ensure sender is a player
        if (!(sender instanceof Player)) {
            ChatUtils.sendMessage(sender, "&4Blad: &cTa komenda moze byc uzywana tylko przez graczy.");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(this.dropMenu.createInventory(player));
        return true;
    }
}