package pl.tenfajnybartek.dropplugin.objects;

/**
 * Reprezentuje bonus do szansy na drop nadawany przez permisję.
 * Użytkownicy z daną permisją otrzymują dodatkową szansę na drop.
 */
public class Chance {
    private final String perm;
    private final Double chance;

    /**
     * Tworzy nowy obiekt Chance.
     * 
     * @param perm Nazwa permisji (np. "drop.vip")
     * @param chance Wartość bonusu do szansy (w procentach)
     */
    public Chance(String perm, Double chance) {
        this.perm = perm;
        this.chance = chance;
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
