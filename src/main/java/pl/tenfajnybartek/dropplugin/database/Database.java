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
import java.util.logging.Logger;

public class Database {
    private static Database database;
    private final DropPlugin plugin;
    private final Logger logger;
    private HikariDataSource ds;
    private String dbType;

    public Database(DropPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        database = this;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            ConfigManager config = plugin.getPluginConfig();
            this.dbType = config.getDbType();
            
            this.logger.info("Łączenie z bazą danych (" + this.dbType.toUpperCase() + ")...");

            String jdbcUrl;
            HikariConfig hikariConfig = new HikariConfig();
            
            if ("sqlite".equalsIgnoreCase(dbType)) {
                java.io.File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                java.io.File dbFile = new java.io.File(dataFolder, "database.db");
                jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                
                hikariConfig.setJdbcUrl(jdbcUrl);
                hikariConfig.setMaximumPoolSize(1);
                hikariConfig.setConnectionTimeout(config.getDbConnectionTimeoutMs());

                hikariConfig.addDataSourceProperty("journal_mode", "WAL");
                hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
                
                this.logger.info("Używam SQLite: " + dbFile.getAbsolutePath());
            } else {
                String host = config.getDbHost();
                int port = config.getDbPort();
                String base = config.getDbBase();
                String user = config.getDbUser();
                String pass = config.getDbPass();
                int maxPool = config.getDbMaxPool() > 0 ? config.getDbMaxPool() : 10;

                jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, base);

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
                
                this.logger.info("Używam MySQL: " + host + ":" + port + "/" + base);
            }

            try {
                ds = new HikariDataSource(hikariConfig);
                this.logger.info("Połączono z bazą danych!");
            } catch (Exception e) {
                this.logger.warning("Nie można połączyć z bazą danych " + dbType.toUpperCase() + "!");
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin((Plugin) plugin));
                return;
            }

            try (Connection conn = ds.getConnection();
                 Statement st = conn.createStatement()) {
                String createTableSQL;
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    createTableSQL = "CREATE TABLE IF NOT EXISTS drop_users (" +
                            "identifier TEXT PRIMARY KEY NOT NULL, " +
                            "cobble INTEGER NOT NULL, " +
                            "messages INTEGER NOT NULL, " +
                            "turboDrop INTEGER NOT NULL, " +
                            "turboExp INTEGER NOT NULL, " +
                            "lvl INTEGER NOT NULL, " +
                            "points INTEGER NOT NULL, " +
                            "minedDrops TEXT NOT NULL, " +
                            "disabledDrops TEXT NOT NULL)";
                } else {
                    createTableSQL = "CREATE TABLE IF NOT EXISTS `drop_users` (" +
                            "identifier VARCHAR(255) NOT NULL, " +
                            "cobble BOOLEAN NOT NULL, " +
                            "messages BOOLEAN NOT NULL, " +
                            "turboDrop BIGINT NOT NULL, " +
                            "turboExp BIGINT NOT NULL, " +
                            "lvl INT NOT NULL, " +
                            "points INT NOT NULL, " +
                            "minedDrops TEXT NOT NULL, " +
                            "disabledDrops TEXT NOT NULL, " +
                            "PRIMARY KEY(identifier))";
                }
                st.executeUpdate(createTableSQL);
                this.logger.info("Tabela drop_users została utworzona/sprawdzona.");
            } catch (SQLException e) {
                this.logger.warning("Nie można utworzyć tabel w bazie danych!");
                e.printStackTrace();
            }

            int loadedUsers = 0;
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM drop_users");
                 ResultSet resultSet = ps.executeQuery()) {
                
                while (resultSet.next()) {
                    try {
                        User user = new User(resultSet);
                        if (plugin.getUserManager().loadUser(user)) {
                            loadedUsers++;
                        } else {
                            this.logger.warning("Użytkownik " + user.getIdentifier() + " już istnieje w cache");
                        }
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
        saveUser(user, false);
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
            String sql;
            if ("sqlite".equalsIgnoreCase(this.dbType)) {
                sql = "INSERT OR REPLACE INTO drop_users (identifier, cobble, messages, turboDrop, turboExp, lvl, points, minedDrops, disabledDrops) VALUES (?,?,?,?,?,?,?,?,?)";
            } else {
                sql = "REPLACE INTO drop_users (identifier, cobble, messages, turboDrop, turboExp, lvl, points, minedDrops, disabledDrops) VALUES (?,?,?,?,?,?,?,?,?)";
            }
            
            try (Connection conn = ds.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

                preparedStatement.setString(1, user.getIdentifier().toString());

                if ("sqlite".equalsIgnoreCase(this.dbType)) {
                    preparedStatement.setInt(2, user.isCobble() ? 1 : 0);
                    preparedStatement.setInt(3, user.isMessages() ? 1 : 0);
                } else {
                    preparedStatement.setBoolean(2, user.isCobble());
                    preparedStatement.setBoolean(3, user.isMessages());
                }
                
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
            task.run();
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void sendEmptyUpdate() {
        if (ds == null) {
            logger.fine("DataSource nie zainicjalizowany - pomijam sendEmptyUpdate");
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = ds.getConnection();
                 PreparedStatement statement = conn.prepareStatement("SELECT 1");
                 ResultSet rs = statement.executeQuery()) {
            } catch (Exception e) {
                this.logger.warning("Nie można zaktualizować bazy danych!");
                e.printStackTrace();
            }
        });
    }

    /**
     * Gets the player name and level at the specified rank position based on level.
     * Rank 1 is the player with the highest level.
     * 
     * @param rank The rank position (1-based)
     * @return The player name with level (e.g., "tenfajnybartek [30]") at that rank, or null if no player exists at that rank
     */
    public String getTopLevelPlayer(int rank) {
        if (rank < 1) {
            return null;
        }
        
        if (ds == null) {
            logger.fine("DataSource nie zainicjalizowany - pomijam getTopLevelPlayer");
            return null;
        }
        
        // SQL query is the same for both SQLite and MySQL
        String sql = "SELECT identifier, lvl FROM drop_users ORDER BY lvl DESC, points DESC LIMIT 1 OFFSET ?";
        
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rank - 1); // OFFSET is 0-based
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String uuidStr = rs.getString("identifier");
                    int level = rs.getInt("lvl");
                    try {
                        java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                        // Try to get the name from Bukkit (works for online/offline players)
                        org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(uuid);
                        String name = offlinePlayer.getName();
                        String playerName = name != null ? name : uuidStr;
                        return playerName + " [" + level + "]";
                    } catch (IllegalArgumentException e) {
                        return uuidStr + " [" + level + "]";
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Nie można pobrać top gracza na pozycji " + rank + ": " + e.getMessage());
        }
        
        return null;
    }

    public void disconnect() {
        disconnect(false);
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
            task.run();
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}