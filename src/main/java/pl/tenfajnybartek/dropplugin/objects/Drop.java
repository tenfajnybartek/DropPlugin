package pl.tenfajnybartek.dropplugin.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Reprezentuje pojedynczy drop, który może wypaść ze stone.
 * Zawiera informacje o itemie, szansie, wysokości, ilości punktów i doświadczenia.
 */
public final class Drop {
    private final ItemStack itemStack;
    private final boolean fortune;
    private final String name;
    private final double chance;
    private final Count height;
    private final Count amount;
    private final Count points;
    private final int exp;
    private final int neededLevel;

    /**
     * Tworzy nowy obiekt Drop.
     * 
     * @param name Nazwa dropu (wyświetlana graczom)
     * @param fortune Czy enchant Fortune zwiększa szansę na ten drop
     * @param itemStack ItemStack który ma wypaść
     * @param chance Szansa na drop (0.0-1.0 lub 0-100)
     * @param height Zakres wysokości Y gdzie drop jest dostępny
     * @param amount Zakres ilości itemów które mogą wypaść
     * @param points Zakres punktów doświadczenia za wykopanie
     * @param exp Ilość Minecraft XP za wykopanie
     * @param neededLevel Minimalny poziom gracza wymagany do odblokowaniа dropu (0 = brak wymagania)
     * @throws IllegalArgumentException jeśli name lub itemStack są null/puste
     */
    public Drop(String name,
                boolean fortune,
                ItemStack itemStack,
                double chance,
                Count height,
                Count amount,
                Count points,
                int exp,
                int neededLevel) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot be null/empty");
        if (itemStack == null) throw new IllegalArgumentException("itemStack cannot be null");

        this.name = name;
        this.fortune = fortune;
        this.itemStack = itemStack.clone();
        this.chance = normalizeChance(chance);
        this.height = height;
        this.amount = amount;
        this.neededLevel = Math.max(0, neededLevel);
        this.points = points;
        
        // Ostrzeżenie gdy exp jest ujemne
        if (exp < 0) {
            java.util.logging.Logger.getLogger("DropPlugin").warning("Drop '" + name + "' ma ujemne exp (" + exp + "), ustawiono na 0");
            this.exp = 0;
        } else {
            this.exp = exp;
        }
    }

    /**
     * Normalizuje wartość szansy do zakresu 0.0-1.0.
     * Wszystkie wartości są traktowane jako procenty i dzielone przez 100.
     * Przykłady:
     * - 1.2 → 0.012 (1.2% szansy)
     * - 50.0 → 0.50 (50% szansy)
     * - 0.5 → 0.005 (0.5% szansy)
     * 
     * @param c Wartość szansy w procentach (0.0-100.0)
     * @return Znormalizowana wartość szansy jako prawdopodobieństwo (0.0-1.0)
     */
    private static double normalizeChance(double c) {
        if (c < 0.0) {
            throw new IllegalArgumentException("chance must be >= 0.0 (percentage value, e.g., 1.2 for 1.2%)");
        }
        if (c > 100.0) {
            throw new IllegalArgumentException("chance must be <= 100.0 (percentage value, max 100%)");
        }
        // Wszystkie wartości są traktowane jako procenty i dzielone przez 100
        return c / 100.0;
    }

    public String getName() {
        return this.name;
    }

    public boolean isFortune() {
        return this.fortune;
    }

    public ItemStack getItemStack() {
        return this.itemStack.clone();
    }

    public double getChance() {
        return this.chance;
    }

    public Count getHeight() {
        return this.height;
    }

    public Count getAmount() {
        return this.amount;
    }

    public Count getPoints() {
        return this.points;
    }

    public int getExp() {
        return this.exp;
    }

    /**
     * @return Minimalny poziom wymagany do odblokowania dropu (0 = brak wymagania)
     */
    public int getNeededLevel() {
        return this.neededLevel;
    }

    /**
     * Sprawdza czy gracz ma wystarczający poziom aby odblokować ten drop.
     * 
     * @param playerLevel Aktualny poziom gracza
     * @return true jeśli drop jest odblokowany dla tego poziomu
     */
    public boolean isUnlocked(int playerLevel) {
        return playerLevel >= this.neededLevel;
    }

    @Override
    public String toString() {
        return "Drop{" +
                "name='" + name + '\'' +
                ", fortune=" + fortune +
                ", chance=" + chance +
                ", height=" + height +
                ", amount=" + amount +
                ", points=" + points +
                ", exp=" + exp +
                ", neededLevel=" + neededLevel +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drop)) return false;
        Drop drop = (Drop) o;
        return fortune == drop.fortune &&
                Double.compare(drop.chance, chance) == 0 &&
                exp == drop.exp &&
                name.equals(drop.name) &&
                Objects.equals(height, drop.height) &&
                Objects.equals(amount, drop.amount) &&
                Objects.equals(points, drop.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fortune, chance, height, amount, points, exp);
    }
}