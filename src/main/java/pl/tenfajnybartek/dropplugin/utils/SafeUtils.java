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

    /**
     * Backwards-compatible: runs initializer, returns result or null on exception.
     */
    public static <T> T safeInit(SafeInitializer<T> initializer) {
        try {
            return initializer.initialize();
        } catch (Exception e) {
            reportUnsafe(e);
            return null;
        }
    }

    /**
     * Preferred: returns Optional.empty() on exception instead of null.
     */
    public static <T> Optional<T> optionalInit(SafeInitializer<T> initializer) {
        try {
            return Optional.ofNullable(initializer.initialize());
        } catch (Exception e) {
            reportUnsafe(e);
            return Optional.empty();
        }
    }

    /**
     * Runs a Runnable-like task that is allowed to throw; exceptions are logged and swallowed.
     */
    public static void safeRun(SafeRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            reportUnsafe(e);
        }
    }

    /**
     * Closes an AutoCloseable quietly (logs exception if thrown).
     */
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