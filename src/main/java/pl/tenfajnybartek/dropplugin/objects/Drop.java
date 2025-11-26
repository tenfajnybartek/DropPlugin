package pl.tenfajnybartek.dropplugin.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Reprezentuje pojedynczy drop.
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

    public Drop(String name,
                boolean fortune,
                ItemStack itemStack,
                double chance,
                Count height,
                Count amount,
                Count points,
                int exp) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot be null/empty");
        if (itemStack == null) throw new IllegalArgumentException("itemStack cannot be null");

        this.name = name;
        this.fortune = fortune;
        this.itemStack = itemStack.clone();
        this.chance = normalizeChance(chance);
        this.height = height;
        this.amount = amount;
        this.points = points;
        this.exp = exp;
    }

    private static double normalizeChance(double c) {
        if (c > 1.0 && c <= 100.0) {
            return c / 100.0;
        }
        if (c < 0.0 || c > 1.0) {
            throw new IllegalArgumentException("chance must be between 0.0 and 1.0 (or 0-100 as percent)");
        }
        return c;
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