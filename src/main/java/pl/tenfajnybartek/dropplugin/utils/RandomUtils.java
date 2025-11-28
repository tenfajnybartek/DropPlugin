package pl.tenfajnybartek.dropplugin.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public final class RandomUtils {
    public static final Random RANDOM_INSTANCE = ThreadLocalRandom.current();

    private RandomUtils() {}

    public static boolean getChance(double chance) {
        if (Double.isNaN(chance) || chance <= 0.0) return false;
        double normalized = chance;
        if (normalized > 1.0) {
            normalized = normalized / 100.0;
        }
        if (normalized >= 1.0) return true;
        return RANDOM_INSTANCE.nextDouble() < normalized;
    }

    public static double getRandDouble(double min, double max) throws IllegalArgumentException {
        if (max < min) throw new IllegalArgumentException("max < min");
        return RANDOM_INSTANCE.nextDouble() * (max - min) + min;
    }

    public static int getRandInt(int min, int max) {
        if (max <= min) return min;
        return RANDOM_INSTANCE.nextInt(max - min + 1) + min;
    }
}