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
            // Sprawdź czy zawiera separator '-'
            if (!trimmed.contains("-")) {
                // Brak minusa - pojedyncza wartość
                int v = Integer.parseInt(trimmed);
                return new Count(v, v);
            }
            
            // Zawiera minus - może to być zakres lub wartość ujemna
            int firstDash = trimmed.indexOf('-');
            
            // Jeśli minus jest na początku, to pierwszy element może być ujemny
            if (firstDash == 0) {
                // Szukamy drugiego minusa (separator zakresu)
                int secondDash = trimmed.indexOf('-', 1);
                
                if (secondDash == -1) {
                    // Tylko jeden minus na początku - pojedyncza wartość ujemna (np. "-5")
                    int v = Integer.parseInt(trimmed);
                    return new Count(v, v);
                }
                
                // Mamy zakres z ujemną wartością początkową (np. "-64-16" lub "-64--32")
                String minStr = trimmed.substring(0, secondDash);
                String maxStr = trimmed.substring(secondDash + 1);
                
                int min = Integer.parseInt(minStr);
                int max = Integer.parseInt(maxStr);
                return new Count(min, max);
            } else {
                // Minus nie jest na początku - zwykły zakres dodatnich liczb (np. "1-5")
                // lub ujemna druga wartość (np. "10--5")
                String[] parts = trimmed.split("-", 2);
                if (parts.length != 2) {
                    return new Count(0, 1);
                }
                
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return new Count(min, max);
            }
        } catch (NumberFormatException ex) {
            // Use warning instead of System.err to avoid Paper's nag message
            java.util.logging.Logger.getLogger("DropPlugin").warning("Nieprawidłowy format Count: '" + toParse + "' - używam domyślnej wartości (0-1)");
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