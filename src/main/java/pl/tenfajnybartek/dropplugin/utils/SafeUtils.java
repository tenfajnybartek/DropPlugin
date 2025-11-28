package pl.tenfajnybartek.dropplugin.utils;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SafeUtils {
    private static final Logger LOGGER = Logger.getLogger(SafeUtils.class.getName());

    private SafeUtils() { }

    private static void reportUnsafe(Throwable th) {
        LOGGER.log(Level.WARNING, "Suppressed exception in SafeUtils", th);
    }

    public static <T> T safeInit(SafeInitializer<T> initializer) {
        try {
            return initializer.initialize();
        } catch (Exception e) {
            reportUnsafe(e);
            return null;
        }
    }

    public static <T> Optional<T> optionalInit(SafeInitializer<T> initializer) {
        try {
            return Optional.ofNullable(initializer.initialize());
        } catch (Exception e) {
            reportUnsafe(e);
            return Optional.empty();
        }
    }

    public static void safeRun(SafeRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            reportUnsafe(e);
        }
    }

    public static void safeClose(AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception e) {
            reportUnsafe(e);
        }
    }

    @FunctionalInterface
    public interface SafeInitializer<T> {
        T initialize() throws Exception;
    }

    @FunctionalInterface
    public interface SafeRunnable {
        void run() throws Exception;
    }
}