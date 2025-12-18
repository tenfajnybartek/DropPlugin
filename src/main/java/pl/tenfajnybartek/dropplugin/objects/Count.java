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
     * Obsługuje wartości ujemne (np. dla wysokości Y od -64 do 320 w Minecraft 1.18+).
     * 
     * @param toParse String do sparsowania (np. "1-5", "3", "-64-90")
     * @return Obiekt Count, lub Count(0, 1) w przypadku błędu
     */
    public static Count parse(String toParse) {
        if (toParse == null || toParse.isBlank()) {
            return new Count(0, 1);
        }
        
        String trimmed = toParse.trim();
        try {
            // Obsługa wartości ujemnych - sprawdź czy zaczyna się od minusa
            if (trimmed.contains("-")) {
                // Dla wartości ujemnych musimy być ostrożni przy split
                // np. "-64-90" powinno dać [-64, 90], nie ["", "64", "90"]
                int firstDash = trimmed.indexOf('-');
                int lastDash = trimmed.lastIndexOf('-');
                
                // Jeśli tylko jeden minus lub minus tylko na początku
                if (firstDash == lastDash) {
                    // Pojedyncza wartość (może być ujemna)
                    int v = Integer.parseInt(trimmed);
                    return new Count(v, v);
                }
                
                // Dwa lub więcej minusów - szukamy separatora
                // Jeśli zaczyna się minusem, to min jest ujemne
                if (firstDash == 0) {
                    // Format: "-X-Y" lub "-X--Y"
                    int separatorIndex = trimmed.indexOf('-', 1);
                    if (separatorIndex == -1) {
                        // Tylko jeden minus na początku
                        int v = Integer.parseInt(trimmed);
                        return new Count(v, v);
                    }
                    String minStr = trimmed.substring(0, separatorIndex).trim();
                    String maxStr = trimmed.substring(separatorIndex + 1).trim();
                    int min = Integer.parseInt(minStr);
                    int max = Integer.parseInt(maxStr);
                    return new Count(min, max);
                } else {
                    // Format: "X-Y" (normalny)
                    String[] split = trimmed.split("-", 2);
                    if (split.length != 2) {
                        return new Count(0, 1);
                    }
                    int min = Integer.parseInt(split[0].trim());
                    int max = Integer.parseInt(split[1].trim());
                    return new Count(min, max);
                }
            } else {
                // Brak minusa - pojedyncza wartość dodatnia
                int v = Integer.parseInt(trimmed);
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