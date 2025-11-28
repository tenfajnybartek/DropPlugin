package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TitleUtils {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private TitleUtils() {}

    public static boolean sendTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        if (player == null) return false;

        String titleStr = title == null ? "" : ChatColor.translateAlternateColorCodes('&', title);
        String subStr = subtitle == null ? "" : ChatColor.translateAlternateColorCodes('&', subtitle);

        try {
            player.sendTitle(titleStr, subStr, Math.max(0, fadeInTicks), Math.max(0, stayTicks), Math.max(0, fadeOutTicks));
            return true;
        } catch (NoSuchMethodError | AbstractMethodError | UnsupportedOperationException ex) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean sendTitleMillis(Player player, String title, String subtitle, long fadeInMs, long stayMs, long fadeOutMs) {
        int fi = msToTicks(fadeInMs);
        int st = msToTicks(stayMs);
        int fo = msToTicks(fadeOutMs);
        return sendTitle(player, title, subtitle, fi, st, fo);
    }

    public static boolean sendActionbar(Player player, String message) {
        if (player == null) return false;
        if (message == null) message = "";

        String coloured = ChatColor.translateAlternateColorCodes('&', message);
        Component comp = LEGACY.deserialize(coloured);

        try {
            Method m = player.getClass().getMethod("sendActionBar", Component.class);
            m.invoke(player, comp);
            return true;
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
        }

        try {
            player.sendTitle("", coloured, 0, 1, 0);
            return true;
        } catch (NoSuchMethodError | AbstractMethodError | UnsupportedOperationException ex) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static int msToTicks(long ms) {
        if (ms <= 0) return 0;
        return (int) Math.ceil(ms / 50.0);
    }
}