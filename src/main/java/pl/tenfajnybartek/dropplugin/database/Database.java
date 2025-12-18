package pl.tenfajnybartek.dropplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.managers.ConfigManager;
import pl.tenfajnybartek.dropplugin.objects.User;
import pl.tenfajnybartek.dropplugin.utils.MapUtils;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

public class Database {
    private static Database database;
    private final DropPlugin plugin;
    private final Logger logger;
    private HikariDataSource ds;

    public Database(DropPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        database = this;

        // Inicjalizacja asynchroniczna
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            this.logger.info("Łączenie z bazą danych...");
            ConfigManager config = plugin.getPluginConfig();

            String host = config.getDbHost();
            int port = config.getDbPort();
            String base = config.getDbBase();
            String user = config.getDbUser();
            String pass = config.getDbPass();
            int maxPool = config.getDbMaxPool() > 0 ? config.getDbMaxPool() : 10;

            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, base);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
            hikariConfig.setMaximumPoolSize(maxPool);
            hikariConfig.setConnectionTimeout(config.getDbConnectionTimeoutMs());
            hikariConfig.setIdleTimeout(config.getDbIdleTimeoutMs());
            if (config.getDbLeakDetectionThresholdMs() > 0) {
                hikariConfig.setLeakDetectionThreshold(config.getDbLeakDetectionThresholdMs());
            }

            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try {
                ds = new HikariDataSource(hikariConfig);
                this.logger.info("Połączono z bazą danych!");
            } catch (Exception e) {
                this.logger.warning("Nie można połączyć z bazą danych!");
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin((Plugin) plugin));
                return;
            }

            // Tworzenie tabeli jeśli nie istnieje
            try (Connection conn = ds.getConnection();
                 Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS `drop_users` (" +
                        "identifier VARCHAR(255) NOT NULL, " +
                        "cobble BOOLEAN NOT NULL, " +
                        "messages BOOLEAN NOT NULL, " +
                        "turboDrop BIGINT(22) NOT NULL, " +
                        "turboExp BIGINT(22) NOT NULL, " +
                        "lvl INT(11) NOT NULL, " +
                        "points INT(11) NOT NULL, " +
                        "minedDrops TEXT NOT NULL, " +
                        "disabledDrops TEXT NOT NULL, " +
                        "PRIMARY KEY(identifier));");
            } catch (SQLException e) {
                this.logger.warning("Nie można utworzyć tabel w bazie danych!");
                e.printStackTrace();
            }

            // Ładowanie użytkowników z bazy
            int loadedUsers = 0;
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM drop_users");
                 ResultSet resultSet = ps.executeQuery()) {
                
                while (resultSet.next()) {
                    try {
                        User user = new User(resultSet);
                        plugin.getUserManager().getUserMap().put(user.getIdentifier(), user);
                        loadedUsers++;
                    } catch (Exception e) {
                        this.logger.warning("Nie można załadować użytkownika: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                this.logger.info("Załadowano " + loadedUsers + " graczy z bazy danych!");
            } catch (SQLException e) {
                this.logger.warning("Nie można załadować graczy z bazy danych!");
                e.printStackTrace();
            }
        });
    }

    public static Database getInstance() {
        return database;
    }

    public void saveUser(User user) {
        saveUser(user, false); // domyślnie async
    }

    public void saveUser(User user, boolean sync) {
        if (user == null) {
            logger.warning("Próba zapisania null użytkownika!");
            return;
        }
        
        if (ds == null) {
            logger.warning("DataSource nie jest zainicjalizowany, pomijam saveUser dla " + user.getIdentifier());
            return;
        }

        Runnable task = () -> {
            String sql = "REPLACE INTO drop_users (identifier, cobble, messages, turboDrop, turboExp, lvl, points, minedDrops, disabledDrops) VALUES (?,?,?,?,?,?,?,?,?)";
            try (Connection conn = ds.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

                preparedStatement.setString(1, user.getIdentifier().toString());
                preparedStatement.setBoolean(2, user.isCobble());
                preparedStatement.setBoolean(3, user.isMessages());
                preparedStatement.setLong(4, user.getTurboDrop());
                preparedStatement.setLong(5, user.getTurboExp());
                preparedStatement.setInt(6, user.getLvl());
                preparedStatement.setInt(7, user.getPoints());
                preparedStatement.setString(8, MapUtils.serializeMap(user.getMinedDrops()));
                preparedStatement.setString(9, MapUtils.serializeList(user.getDisabledDrops()));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    logger.fine("Zapisano użytkownika " + user.getIdentifier() + " (poziom: " + user.getLvl() + ", punkty: " + user.getPoints() + ")");
                }
            } catch (SQLException e) {
                this.logger.warning("Nie można zapisać gracza " + user.getIdentifier() + " do bazy danych!");
                e.printStackTrace();
            }
        };

        if (sync) {
            task.run(); // natychmiast, bez schedulerów!
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Wykonuje SELECT 1 aby utrzymać połączenie (asynchronicznie).
     */
    public void sendEmptyUpdate() {
        if (ds == null) {
            logger.fine("DataSource nie zainicjalizowany - pomijam sendEmptyUpdate");
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement statement = conn.prepareStatement("SELECT 1");
                 ResultSet rs = statement.executeQuery()) {
                // nic do zrobienia, po prostu zapytanie
            } catch (Exception e) {
                this.logger.warning("Nie można zaktualizować bazy danych!");
                e.printStackTrace();
            }
        });
    }

    public void disconnect() {
        disconnect(false); // domyślnie async
    }

    public void disconnect(boolean sync) {
        Runnable task = () -> {
            try {
                if (ds != null && !ds.isClosed()) {
                    ds.close();
                    logger.info("Pula Hikari została zamknięta.");
                }
            } catch (Exception e) {
                logger.warning("Nie można zamknąć połączenia z bazą danych!");
                e.printStackTrace();
            }
        };

        if (sync) {
            task.run(); // NATYCHMIASTOWE wykonie kodu bez schedulerów (SAFE na wyłączaniu pluginu)
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}