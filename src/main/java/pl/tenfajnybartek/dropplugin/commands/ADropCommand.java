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

public class ADropCommand implements CommandExecutor {
    // Limity dla komendy /adrop level
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 1000;
    private static final int MIN_POINTS = 0;
    private static final int MAX_POINTS = 1000000;
    
    private final UserManager userManager;
    private final DropPlugin plugin;

    public ADropCommand(DropPlugin plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();

        if (plugin.getCommand("adrop") != null) {
            plugin.getCommand("adrop").setExecutor(this);
        } else {
            plugin.getLogger().warning("Command 'adrop' not defined in plugin.yml");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var config = this.plugin.getPluginConfig();
        
        if (!sender.hasPermission("dropplugin.cmd.adrop")) {
            ChatUtils.sendMessage(sender, config.getCmdADropNoPermission());
            return true;
        }

        if (args.length < 1) {
            config.getCmdADropUsage().forEach(m -> ChatUtils.sendMessage(sender, m));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload": {
                long start = System.currentTimeMillis();
                ChatUtils.sendMessage(sender, config.getCmdADropReloading());
                try {
                    this.plugin.reloadConfigManager();
                    float time = (float) (System.currentTimeMillis() - start) / 1000.0f;
                    ChatUtils.sendMessage(sender, config.getCmdADropReloaded().replace("{TIME}", String.valueOf(time)));
                } catch (Throwable t) {
                    ChatUtils.sendMessage(sender, config.getCmdADropReloadError());
                    this.plugin.getLogger().severe("Error while reloading config: " + t.getMessage());
                    t.printStackTrace();
                }
                return true;
            }

            case "drop":
            case "exp": {
                if (args.length < 3) {
                    config.getCmdADropUsage().forEach(m -> ChatUtils.sendMessage(sender, m));
                    return true;
                }
                String target = args[1];
                String timeStr = args[2];
                long time = DataUtils.parseDateDiff(timeStr, true);
                if (time <= 0L) {
                    ChatUtils.sendMessage(sender, config.getCmdADropInvalidTime().replace("{TIME}", timeStr));
                    return true;
                }

                boolean isDrop = sub.equals("drop");
                String typeName = isDrop ? "TurboDrop" : "TurboExp";

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
                    ChatUtils.sendMessage(sender, config.getCmdADropTurboEnabled()
                            .replace("{TYPE}", typeName)
                            .replace("{TIME}", timeStr));
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer == null) {
                    ChatUtils.sendMessage(sender, config.getCmdADropPlayerOffline());
                    return true;
                }

                User user = this.userManager.getUser(targetPlayer);
                if (user == null) {
                    ChatUtils.sendMessage(sender, config.getCmdADropPlayerDataError());
                    return true;
                }

                if (isDrop) {
                    user.addTurboDrop(time);
                } else {
                    user.addTurboExp(time);
                }

                ChatUtils.sendMessage(targetPlayer, config.getTurboGetMessage().replace("{TYPE}", typeName).replace("{TIME}", timeStr));
                ChatUtils.sendMessage(sender, config.getCmdADropTurboEnabledFor()
                        .replace("{TYPE}", typeName)
                        .replace("{TIME}", timeStr)
                        .replace("{PLAYER}", targetPlayer.getName()));
                return true;
            }

            case "level": {
                if (args.length < 3) {
                    config.getCmdADropUsage().forEach(m -> ChatUtils.sendMessage(sender, m));
                    return true;
                }

                String targetName = args[1];
                int newLevel;
                int newPoints = 0;

                try {
                    newLevel = Integer.parseInt(args[2]);
                    if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL) {
                        ChatUtils.sendMessage(sender, config.getCmdADropLevelMinMax()
                                .replace("{MIN}", String.valueOf(MIN_LEVEL))
                                .replace("{MAX}", String.valueOf(MAX_LEVEL)));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    ChatUtils.sendMessage(sender, config.getCmdADropLevelNotNumber());
                    return true;
                }

                if (args.length >= 4) {
                    try {
                        newPoints = Integer.parseInt(args[3]);
                        if (newPoints < MIN_POINTS || newPoints > MAX_POINTS) {
                            ChatUtils.sendMessage(sender, config.getCmdADropPointsMinMax()
                                    .replace("{MIN}", String.valueOf(MIN_POINTS))
                                    .replace("{MAX}", String.valueOf(MAX_POINTS)));
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        ChatUtils.sendMessage(sender, config.getCmdADropPointsNotNumber());
                        return true;
                    }
                }

                Player targetPlayer = Bukkit.getPlayer(targetName);
                if (targetPlayer == null) {
                    ChatUtils.sendMessage(sender, config.getCmdADropPlayerOffline());
                    return true;
                }

                User targetUser = this.userManager.getUser(targetPlayer);
                if (targetUser == null) {
                    ChatUtils.sendMessage(sender, config.getCmdADropPlayerDataError());
                    return true;
                }

                int currentLevel = targetUser.getLvl();
                targetUser.setLvl(newLevel);
                targetUser.setPoints(newPoints);

                ChatUtils.sendMessage(sender, config.getCmdADropLevelChanged()
                        .replace("{PLAYER}", targetPlayer.getName())
                        .replace("{OLD_LEVEL}", String.valueOf(currentLevel))
                        .replace("{NEW_LEVEL}", String.valueOf(newLevel))
                        .replace("{POINTS}", String.valueOf(newPoints)));
                return true;
            }

            default: {
                config.getCmdADropUsage().forEach(m -> ChatUtils.sendMessage(sender, m));
                return true;
            }
        }
    }
}