package pl.tenfajnybartek.dropplugin.objects;

/**
 * Reprezentuje bonus do szansy na drop nadawany przez permisję.
 * Użytkownicy z daną permisją otrzymują dodatkową szansę na drop.
 */
public class Chance {
    private final String name;
    private final String perm;
    private final Double chance;

    /**
     * Tworzy nowy obiekt Chance z nazwą.
     * 
     * @param name Nazwa bonusu (np. "vip")
     * @param perm Nazwa permisji (np. "dropplugin.vip")
     * @param chance Wartość bonusu do szansy (w procentach, np. 0.5 = +0.5% szansy)
     */
    public Chance(String name, String perm, Double chance) {
        this.name = name;
        this.perm = perm;
        this.chance = chance;
    }

    /**
     * Konstruktor dla wstecznej kompatybilności.
     * 
     * @param perm Nazwa permisji (np. "drop.vip")
     * @param chance Wartość bonusu do szansy (w procentach)
     */
    public Chance(String perm, Double chance) {
        this.name = perm;
        this.perm = perm;
        this.chance = chance;
    }

    /**
     * @return Nazwa bonusu
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return Nazwa permisji
     */
    public String getPerm() {
        return this.perm;
    }

    /**
     * @return Wartość bonusu do szansy (w procentach)
     */
    public Double getChance() {
        return this.chance;
    }
}
