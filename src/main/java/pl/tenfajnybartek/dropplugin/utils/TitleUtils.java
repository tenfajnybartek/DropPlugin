package pl.tenfajnybartek.dropplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Kompatybilne utils do wysyłania title/actionbar bez używania net.kyori.adventure.title.Title.
 *
 * - do tytułów używamy stabilnego Bukkitowego API: Player#sendTitle(String,String,int,int,int)
 * - do actionbara próbujemy wywołać (przez refleksję) Player#sendActionBar(Component) jeśli jest dostępne
 *   (unika to kompilacji zależnej od różnych wersji Adventure/Title),
 *   a jeśli nie ma tej metody — fallbackem jest krótkie wyświetlenie subtitle przez Player#sendTitle(...) (1 tick).
 *
 * Dzięki temu unikamy błędów kompilacji związanych z różnymi wersjami net.kyori.adventure.title.Title
 * oraz z ostrzeżeniami typu "scheduled for removal".
 */
public final class TitleUtils {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private TitleUtils() {}

    /**
     * Wyślij tytuł (używa Bukkitowego API stringowego). Czas w tickach (1 tick = 50 ms).
     */
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

    /**
     * Wrapper akceptujący czasy w milisekundach (konwertuje na ticki).
     */
    public static boolean sendTitleMillis(Player player, String title, String subtitle, long fadeInMs, long stayMs, long fadeOutMs) {
        int fi = msToTicks(fadeInMs);
        int st = msToTicks(stayMs);
        int fo = msToTicks(fadeOutMs);
        return sendTitle(player, title, subtitle, fi, st, fo);
    }

    /**
     * Wyślij actionbar:
     * - próbujemy wywołać Player#sendActionBar(Component) przez refleksję (bez kompilowania zależności),
     * - jeśli metoda nie istnieje -> fallback: krótkie wyświetlenie subtitle jako actionbar (1 tick) przez Player#sendTitle.
     *
     * Nie używamy przestarzałych TextComponentów jako fallback.
     */
    public static boolean sendActionbar(Player player, String message) {
        if (player == null) return false;
        if (message == null) message = "";

        // przygotuj Component (z & -> kolory)
        String coloured = ChatColor.translateAlternateColorCodes('&', message);
        Component comp = LEGACY.deserialize(coloured);

        // Spróbuj przez refleksję wywołać player.sendActionBar(Component)
        try {
            Method m = player.getClass().getMethod("sendActionBar", Component.class);
            m.invoke(player, comp);
            return true;
        } catch (NoSuchMethodException e) {
            // metoda niedostępna w runtime -> fallback poniżej
        } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
            // jeśli wywołanie się nie powiodło, spróbuj fallback
        }

        // Fallback: użyj krótkiego sendTitle jako substytutu actionbar (stay = 1 tick)
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