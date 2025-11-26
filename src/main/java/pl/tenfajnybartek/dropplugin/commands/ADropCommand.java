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
import pl.tenfajnybartek.dropplugin.utils.DataUtils;

import java.util.Arrays;
import java.util.List;

public class ADropCommand implements CommandExecutor {
    private final List<String> usage;
    private final UserManager userManager;
    private final DropPlugin plugin;

    public ADropCommand(DropPlugin plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.usage = Arrays.asList(
                "&6Dostepne komendy dla adminow",
                "&&6/adrop <drop/exp> <all> <time> &7- Nadaje turbo dla calego serwera.",
                "&6/adrop <drop/exp> <player> <time> &7- Nadaje turbo dla jednego gracza.",
                "&6/adrop reload - &7Przeladowywuje config",
                "&6/adrop level <nick_gracza> <poziom> <pkt> &7- Ustawia poziom gracza.",
                "&7Standardowo 1 lvl - 0pkt, pamietaj x lvl * 500."
        );

        if (plugin.getCommand("adrop") != null) {
            plugin.getCommand("adrop").setExecutor(this);
        } else {
            plugin.getLogger().warning("Command 'adrop' not defined in plugin.yml");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dropplugin.cmd.adrop")) {
            ChatUtils.sendMessage(sender, "&4Blad: &cNie masz uprawnien do tej komendy! &7(dropplugin.cmd.adrop)");
            return true;
        }

        if (args.length < 1) {
            this.usage.forEach(m -> ChatUtils.sendMessage(sender, m));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload": {
                long start = System.currentTimeMillis();
                ChatUtils.sendMessage(sender, "&7Przeladowywanie...");
                // Rekreujemy ConfigManager w pluginie (DropPlugin.reloadConfigManager())
                try {
                    this.plugin.reloadConfigManager();
                    ChatUtils.sendMessage(sender, "&eDrop &7przeladowano! (" + (float) (System.currentTimeMillis() - start) / 1000.0f + "s)");
                } catch (Throwable t) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cNie udalo sie przeladowac konfiguracji.");
                    this.plugin.getLogger().severe("Error while reloading config: " + t.getMessage());
                    t.printStackTrace();
                }
                return true;
            }

            case "drop":
            case "exp": {
                if (args.length < 3) {
                    this.usage.forEach(m -> ChatUtils.sendMessage(sender, m));
                    return true;
                }

                String target = args[1];
                String timeStr = args[2];
                long time = DataUtils.parseDateDiff(timeStr, true);
                if (time <= 0L) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cNiepoprawny czas: " + timeStr);
                    return true;
                }

                boolean isDrop = sub.equals("drop");
                String typeName = isDrop ? "TurboDrop" : "TurboExp";

                // pobieraj konfigurację zawsze z pluginu (żeby reload działał od razu)
                var config = this.plugin.getPluginConfig();

                if ("all".equalsIgnoreCase(target)) {
                    if (isDrop) {
                        config.addTurboDrop(time);
                        config.save();
                    } else {
                        config.addTurboExp(time);
                        config.save();
                    }

                    String broadcast = config.getTurboMessage()
                            .replace("{ADMIN}", sender.getName())
                            .replace("{TYPE}", typeName)
                            .replace("{TIME}", timeStr);
                    Bukkit.getOnlinePlayers().forEach(player -> ChatUtils.sendMessage(player, broadcast));
                    ChatUtils.sendMessage(sender, "&aWlaczyles " + typeName + " na: " + timeStr);
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer == null) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cGracz jest offline!");
                    return true;
                }

                User user = this.userManager.getUser(targetPlayer);
                if (user == null) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cNie mozna zaladowac danych gracza!");
                    return true;
                }

                if (isDrop) {
                    user.addTurboDrop(time);
                } else {
                    user.addTurboExp(time);
                }

                ChatUtils.sendMessage(targetPlayer, config.getTurboGetMessage().replace("{TYPE}", typeName).replace("{TIME}", timeStr));
                ChatUtils.sendMessage(sender, "&aWlaczyles " + typeName + " na: " + timeStr + " dla: " + targetPlayer.getName());
                return true;
            }

            case "level": {
                if (args.length < 3) {
                    this.usage.forEach(m -> ChatUtils.sendMessage(sender, m));
                    return true;
                }

                String targetName = args[1];
                int newLevel;
                int newPoints = 0;

                try {
                    newLevel = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cPodany poziom nie jest liczba!");
                    return true;
                }

                if (args.length >= 4) {
                    try {
                        newPoints = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        ChatUtils.sendMessage(sender, "&4Blad: &cPodane punkty nie sa liczba!");
                        return true;
                    }
                }

                Player targetPlayer = Bukkit.getPlayer(targetName);
                if (targetPlayer == null) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cGracz jest offline!");
                    return true;
                }

                User targetUser = this.userManager.getUser(targetPlayer);
                if (targetUser == null) {
                    ChatUtils.sendMessage(sender, "&4Blad: &cNie mozna zaladowac danych gracza!");
                    return true;
                }

                int currentLevel = targetUser.getLvl();
                targetUser.setLvl(newLevel);
                targetUser.setPoints(newPoints);

                ChatUtils.sendMessage(sender,
                        "&aZmieniono poziom gracza &7" + targetPlayer.getName() +
                                " &az &7" + currentLevel + " &ana &7" + newLevel +
                                " &ai ustawiono punkty na &7" + newPoints);
                return true;
            }

            default: {
                this.usage.forEach(m -> ChatUtils.sendMessage(sender, m));
                return true;
            }
        }
    }
}