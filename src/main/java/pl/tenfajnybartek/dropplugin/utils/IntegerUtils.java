package pl.tenfajnybartek.dropplugin.utils;

public final class IntegerUtils {
    private IntegerUtils() {}

    public static boolean isInt(String string) {
        if (string == null) return false;
        String s = string.trim();
        if (s.isEmpty()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Integer parseOrNull(String string) {
        if (string == null) return null;
        String s = string.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int parseOrDefault(String string, int defaultValue) {
        Integer v = parseOrNull(string);
        return v == null ? defaultValue : v;
    }

    public static boolean isNonNegativeInt(String string) {
        Integer v = parseOrNull(string);
        return v != null && v >= 0;
    }

    public static boolean isPositiveInt(String string) {
        Integer v = parseOrNull(string);
        return v != null && v > 0;
    }
}