package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class ChatUtils {
    private ChatUtils() {}

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static String colour(String contentMessage) {
        if (contentMessage == null) return "";
        Component comp = LEGACY_SERIALIZER.deserialize(contentMessage);
        return LEGACY_SERIALIZER.serialize(comp);
    }

    public static List<String> colour(List<String> list) {
        if (list == null) return List.of();
        return list.stream().map(ChatUtils::colour).collect(Collectors.toList());
    }

    public static Component toComponent(String text) {
        if (text == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(text);
    }

    public static boolean sendMessage(Player player, String message) {
        if (player == null) return false;
        player.sendMessage(toComponent(message));
        return true;
    }

    public static boolean sendMessage(CommandSender commandSender, String message) {
        if (commandSender == null) return false;
        if (commandSender instanceof Player) {
            return sendMessage((Player) commandSender, message);
        } else {
            commandSender.sendMessage(colour(message));
            return true;
        }
    }
}