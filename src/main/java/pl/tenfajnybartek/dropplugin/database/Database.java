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

        // Inicjalizujemy asynchronicznie
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            this.logger.info("Laczenie z baza danych...");
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

            // Dodatkowe rekomendowane ustawienia
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            try {
                ds = new HikariDataSource(hikariConfig);
                this.logger.info("Polaczono z baza danych!");
            } catch (Exception e) {
                this.logger.warning("Nie mozna polaczyc z baza danych!");
                e.printStackTrace();
                // jeśli nie można się połączyć - wyłączamy plugin bez używania przestarzałego API
                // Wywołanie disablePlugin musi być wykonane na głównym wątku, dlatego schedulujemy zadanie synchroniczne.
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin((Plugin) plugin));
                return;
            }

            // Tworzymy tabelę (jeśli nie istnieje)
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
                        "lastMessage VARCHAR(255), " +
                        "lastSender VARCHAR(255), " +
                        "PRIMARY KEY(identifier));");
            } catch (SQLException e) {
                this.logger.warning("Nie mozna utworzyc tabel w bazie danych!");
                e.printStackTrace();
            }

            // Ładujemy użytkowników
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM drop_users");
                 ResultSet resultSet = ps.executeQuery()) {

                while (resultSet.next()) {
                    try {
                        // Avoid shadowing variable names from outer scopes: use 'dbUser' instead of 'user'
                        User dbUser = new User(resultSet);
                        // lastMessage może być null
                        dbUser.setLastMessage(resultSet.getString("lastMessage"));
                        String lastSenderStr = resultSet.getString("lastSender");
                        if (lastSenderStr != null && !lastSenderStr.isEmpty()) {
                            try {
                                dbUser.setLastSender(UUID.fromString(lastSenderStr));
                            } catch (IllegalArgumentException iae) {
                                logger.warning("Niepoprawny lastSender UUID dla gracza " + dbUser.getIdentifier() + ": " + lastSenderStr);
                            }
                        }
                        plugin.getUserManager().getUserMap().put(dbUser.getIdentifier(), dbUser);
                    } catch (Exception e) {
                        logger.warning("Blad przy parsowaniu rekordu uzytkownika: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                this.logger.warning("Nie mozna zaladowac graczy z bazy danych!");
                e.printStackTrace();
            } finally {
                this.logger.info("Zaladowano " + plugin.getUserManager().getUserMap().size() + " graczy!");
            }
        });
    }

    public static Database getInstance() {
        return database;
    }

    /**
     * Zapisuje użytkownika asynchronicznie.
     */
    public void saveUser(User user) {
        if (ds == null) {
            logger.warning("DataSource nie jest zainicjalizowany, pomijam saveUser dla " + user.getIdentifier());
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "REPLACE INTO drop_users (identifier, cobble, messages, turboDrop, turboExp, lvl, points, minedDrops, disabledDrops, lastMessage, lastSender) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
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
                preparedStatement.setString(10, user.getLastMessage());
                if (user.getLastSender() != null) {
                    preparedStatement.setString(11, user.getLastSender().toString());
                } else {
                    preparedStatement.setNull(11, Types.VARCHAR);
                }

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                this.logger.warning("Nie mozna zapisac gracza do bazy danych!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Wykonuje prosty SELECT 1 aby utrzymać połączenie (asynchronicznie).
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
                this.logger.warning("Nie mozna zaktualizowac bazy danych!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Zamknięcie puli Hikari (asynchronicznie).
     */
    public void disconnect() {
        // Zamykamy asynchronicznie aby nie blokować głównego wątku
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (ds != null && !ds.isClosed()) {
                    ds.close();
                    logger.info("Pula Hikari zostala zamknieta.");
                }
            } catch (Exception e) {
                logger.warning("Nie mozna zamknac polaczenia z baza danych!");
                e.printStackTrace();
            }
        });
    }
}