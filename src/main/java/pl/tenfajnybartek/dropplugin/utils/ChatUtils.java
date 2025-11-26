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

    /**
     * Zwraca tekst z zamienionymi kodami '&' na legacy (znak §) jako String.
     * Używane tam, gdzie wymagany jest String (np. zapisy do pliku/console).
     */
    public static String colour(String contentMessage) {
        if (contentMessage == null) return "";
        // deserialize -> component -> serialize, aby poprawnie obsłużyć wszystkie kody legacy (&)
        Component comp = LEGACY_SERIALIZER.deserialize(contentMessage);
        return LEGACY_SERIALIZER.serialize(comp);
    }

    /**
     * Zwraca listę Stringów z zamienionymi kodami '&' na legacy (znak §).
     */
    public static List<String> colour(List<String> list) {
        if (list == null) return List.of();
        return list.stream().map(ChatUtils::colour).collect(Collectors.toList());
    }

    /**
     * Konwertuje tekst (z &-kodami) na Adventure Component przy użyciu Legacy serializera.
     */
    public static Component toComponent(String text) {
        if (text == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(text);
    }

    /**
     * Wysyła wiadomość do gracza używając Component (Adventure).
     * Zwraca true dla zgodności z wcześniejszą sygnaturą.
     */
    public static boolean sendMessage(Player player, String message) {
        if (player == null) return false;
        player.sendMessage(toComponent(message));
        return true;
    }

    /**
     * Wysyła wiadomość do dowolnego CommandSender.
     * - jeżeli to Player -> użyjemy Component (Adventure)
     * - w przeciwnym razie wyślemy kolorowany String (z kodami §)
     */
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