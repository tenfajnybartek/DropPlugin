package pl.tenfajnybartek.dropplugin.objects;


public class Chance {
    private final String name;
    private final String perm;
    private final Double chance;

    public Chance(String name, String perm, Double chance) {
        this.name = name;
        this.perm = perm;
        this.chance = chance;
    }

    public Chance(String perm, Double chance) {
        this.name = perm;
        this.perm = perm;
        this.chance = chance;
    }

    public String getName() {
        return this.name;
    }

    public String getPerm() {
        return this.perm;
    }

    public Double getChance() {
        return this.chance;
    }
}
