package pl.tenfajnybartek.dropplugin.objects;

public class Chance {
    private final String perm;
    private final Double chance;

    public Chance(String perm, Double chance) {
        this.perm = perm;
        this.chance = chance;
    }

    public String getPerm() {
        return this.perm;
    }

    public Double getChance() {
        return this.chance;
    }
}
