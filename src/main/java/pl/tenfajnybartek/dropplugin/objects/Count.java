package pl.tenfajnybartek.dropplugin.objects;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class Count {
    private final int min;
    private final int max;

    public Count(int min, int max) {
        if (max < min) max = min;
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int random() {
        if (this.min == this.max) return this.min;
        return ThreadLocalRandom.current().nextInt(this.min, this.max + 1);
    }

    @Override
    public String toString() {
        return this.min + "-" + this.max;
    }

    public static Count parse(String toParse) {
        if (toParse == null || toParse.isBlank()) return new Count(0, 1);
        String trimmed = toParse.trim();
        try {
            if (trimmed.contains("-")) {
                String[] split = trimmed.split("-", 2);
                int min = Integer.parseInt(split[0].trim());
                int max = Integer.parseInt(split[1].trim());
                return new Count(min, max);
            } else {
                int v = Integer.parseInt(trimmed);
                return new Count(v, v);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return new Count(0, 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Count)) return false;
        Count count = (Count) o;
        return min == count.min && max == count.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}