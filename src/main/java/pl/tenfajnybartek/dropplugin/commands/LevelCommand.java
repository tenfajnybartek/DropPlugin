package pl.tenfajnybartek.dropplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.UserManager;
import pl.tenfajnybartek.dropplugin.objects.User;
import pl.tenfajnybartek.dropplugin.utils.ChatUtils;

public class LevelCommand implements CommandExecutor {

    private final UserManager userManager;

    public LevelCommand(DropPlugin plugin) {
        this.userManager = plugin.getUserManager();
        if (plugin.getCommand("level") != null) {
            plugin.getCommand("level").setExecutor(this);
        } else {
            plugin.getLogger().warning("Command 'level' is not defined in plugin.yml");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tfbhc.cmd.level")) {
            ChatUtils.sendMessage(sender, "&4Blad: &cNie masz uprawnien do tej komendy! &7(tfbhc.cmd.level)");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                ChatUtils.sendMessage(sender, "&4Blad: &cTa komenda moze byc uzyta tylko przez graczy.");
                return true;
            }
            Player player = (Player) sender;
            User u = this.userManager.getUser(player);
            if (u == null) {
                ChatUtils.sendMessage(sender, "&4Blad: &cNie mozna zaladowac Twoich danych!");
                return true;
            }
            displayPlayerInfo(sender, u);
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("tfbhc.cmd.alevel")) {
                ChatUtils.sendMessage(sender, "&4Blad: &cNie masz uprawnien do sprawdzania innych graczy! &7(tfbhc.cmd.alevel)");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                ChatUtils.sendMessage(sender, "&4Blad: &cPodany gracz nie jest online!");
                return true;
            }

            User targetUser = this.userManager.getUser(targetPlayer);
            if (targetUser == null) {
                ChatUtils.sendMessage(sender, "&4Blad: &cNie mozna zaladowac danych podanego gracza!");
                return true;
            }

            displayPlayerInfo(sender, targetUser);
            return true;
        }

        // niepoprawna liczba argumentów
        ChatUtils.sendMessage(sender, "&cPoprawne uzycie: &7/level [nick_gracza]");
        return true;
    }

    private void displayPlayerInfo(CommandSender sender, User user) {
        if (user == null || user.getPlayer() == null) {
            ChatUtils.sendMessage(sender, "&4Blad: &cBrak danych gracza.");
            return;
        }

        ChatUtils.sendMessage(sender, "&8&m-----------------&8[ &f&lPOZIOM GRACZA &8]&8&m-----------------");
        ChatUtils.sendMessage(sender, " ");
        ChatUtils.sendMessage(sender, " &8* &7Nick: &e" + user.getPlayer().getName());
        ChatUtils.sendMessage(sender, " &8* &7Aktualny poziom kopania: &a" + user.getLvl() + " &7, punkty: &6" + user.getPoints());
        ChatUtils.sendMessage(sender, " &8* &7Do nastepnego poziomu brakuje: &c" + user.getPointsToNextLevel());
        ChatUtils.sendMessage(sender, " ");
        ChatUtils.sendMessage(sender, "&8&m-----------------&8[ &f&lPOZIOM GRACZA &8]&8&m-----------------");
    }
}