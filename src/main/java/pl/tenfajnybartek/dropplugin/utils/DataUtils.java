package pl.tenfajnybartek.dropplugin.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DataUtils {
    private DataUtils() {}

    public static String durationToString(long time) {
        long diff = time - System.currentTimeMillis();
        if (diff <= 0L) {
            return "0s";
        }
        StringBuilder sb = new StringBuilder();
        long days = diff / 86_400_000L;
        long hours = (diff / 3_600_000L) % 24L;
        long minutes = (diff / 60_000L) % 60L;
        long seconds = (diff / 1000L) % 60L;
        if (days > 0L) {
            sb.append(days).append("d");
        }
        if (hours > 0L) {
            sb.append(hours).append("h");
        }
        if (minutes > 0L) {
            sb.append(minutes).append("min");
        }
        if (seconds > 0L) {
            sb.append(seconds).append("s");
        }
        return sb.toString();
    }

    /**
     * Parsuje wyrażenie czasu typu "1y2mo3w4d5h6m7s" (kolejność dowolna, części opcjonalne).
     * Jeżeli future == true, zwraca timestamp w przyszłości; w przeciwnym razie w przeszłości.
     * Zwraca -1 przy błędzie parsowania.
     */
    public static long parseDateDiff(String time, boolean future) {
        if (time == null || time.isBlank()) return -1L;
        try {
            // case-insensitive
            Pattern timePattern = Pattern.compile(
                    "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" +    // years
                            "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +   // months
                            "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" +    // weeks
                            "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +    // days
                            "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +    // hours
                            "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +    // minutes
                            "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

            Matcher m = timePattern.matcher(time.trim());
            int years = 0, months = 0, weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
            boolean found = false;
            if (m.find()) {
                // If the whole match is empty, treat as not found
                if (m.group() == null || m.group().isEmpty()) {
                    return -1L;
                }
                // groups 1..7 correspond to the capture groups above
                if (m.group(1) != null && !m.group(1).isEmpty()) years = Integer.parseInt(m.group(1));
                if (m.group(2) != null && !m.group(2).isEmpty()) months = Integer.parseInt(m.group(2));
                if (m.group(3) != null && !m.group(3).isEmpty()) weeks = Integer.parseInt(m.group(3));
                if (m.group(4) != null && !m.group(4).isEmpty()) days = Integer.parseInt(m.group(4));
                if (m.group(5) != null && !m.group(5).isEmpty()) hours = Integer.parseInt(m.group(5));
                if (m.group(6) != null && !m.group(6).isEmpty()) minutes = Integer.parseInt(m.group(6));
                if (m.group(7) != null && !m.group(7).isEmpty()) seconds = Integer.parseInt(m.group(7));
                found = true;
            }

            if (!found) return -1L;

            GregorianCalendar c = new GregorianCalendar();
            int sign = future ? 1 : -1;
            if (years > 0) c.add(Calendar.YEAR, years * sign);
            if (months > 0) c.add(Calendar.MONTH, months * sign);
            if (weeks > 0) c.add(Calendar.WEEK_OF_YEAR, weeks * sign);
            if (days > 0) c.add(Calendar.DAY_OF_MONTH, days * sign);
            if (hours > 0) c.add(Calendar.HOUR_OF_DAY, hours * sign);
            if (minutes > 0) c.add(Calendar.MINUTE, minutes * sign);
            if (seconds > 0) c.add(Calendar.SECOND, seconds * sign);

            // limit do 10 lat od teraz
            GregorianCalendar max = new GregorianCalendar();
            max.add(Calendar.YEAR, 10);
            if (future && c.after(max)) {
                return max.getTimeInMillis();
            }
            return c.getTimeInMillis();
        } catch (Exception e) {
            return -1L;
        }
    }
}