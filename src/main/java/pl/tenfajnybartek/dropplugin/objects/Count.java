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

    /**
     * Parsuje string w formacie "min-max" lub pojedynczą wartość do obiektu Count.
     * 
     * @param toParse String do sparsowania (np. "1-5" lub "3")
     * @return Obiekt Count, lub Count(0, 1) w przypadku błędu
     */
    public static Count parse(String toParse) {
        if (toParse == null || toParse.isBlank()) {
            return new Count(0, 1);
        }
        
        String trimmed = toParse.trim();
        try {
            if (trimmed.contains("-")) {
                String[] split = trimmed.split("-", 2);
                if (split.length != 2) {
                    return new Count(0, 1);
                }
                int min = Integer.parseInt(split[0].trim());
                int max = Integer.parseInt(split[1].trim());
                // Zabezpieczenie przed nieprawidłowymi wartościami
                if (min < 0) min = 0;
                if (max < 0) max = 0;
                return new Count(min, max);
            } else {
                int v = Integer.parseInt(trimmed);
                if (v < 0) v = 0;
                return new Count(v, v);
            }
        } catch (NumberFormatException ex) {
            System.err.println("Nieprawidłowy format Count: " + toParse);
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