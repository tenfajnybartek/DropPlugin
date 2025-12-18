package pl.tenfajnybartek.dropplugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;
import pl.tenfajnybartek.dropplugin.objects.Chance;
import pl.tenfajnybartek.dropplugin.utils.ItemBuilder;

import java.util.*;

public class ConfigManager {
    private final HashMap<String, Chance> chances;
    private final String dbType;
    private final String dbHost;
    private final String dbUser;
    private final String dbPass;
    private final String dbBase;
    private final String dropMessage;
    private final String lvlUpMessage;
    private final String turboGetMessage;
    private final String turboMessage;
    private final String guiFillerName;
    private final String guiName;
    private final String guiStatusOn;
    private final String guiStatusOff;
    private final String guiItemName;
    private final String guiLockedName;
    private final String guiCobbleName;
    private final String guiMessagesName;
    private final String guiTurboName;
    private final String guiLevelName;
    private final String enableAllName;
    private final String disableAllName;
    private final String messageInvFull;
    private final String maxLevelMessage;
    private final String levelMessage;
    private final String actionbarMessage;
    
    // Command messages - DropCommand
    private final String cmdDropNoPermission;
    private final String cmdDropOnlyPlayers;
    
    // Command messages - ADropCommand
    private final String cmdADropNoPermission;
    private final List<String> cmdADropUsage;
    private final String cmdADropReloading;
    private final String cmdADropReloaded;
    private final String cmdADropReloadError;
    private final String cmdADropInvalidTime;
    private final String cmdADropTurboEnabled;
    private final String cmdADropTurboEnabledFor;
    private final String cmdADropPlayerOffline;
    private final String cmdADropPlayerDataError;
    private final String cmdADropLevelMinMax;
    private final String cmdADropLevelNotNumber;
    private final String cmdADropPointsMinMax;
    private final String cmdADropPointsNotNumber;
    private final String cmdADropLevelChanged;
    
    // Command messages - LevelCommand
    private final String cmdLevelNoPermission;
    private final String cmdLevelNoALevelPermission;
    private final String cmdLevelOnlyPlayers;
    private final String cmdLevelPlayerDataError;
    private final String cmdLevelTargetPlayerDataError;
    private final String cmdLevelPlayerOffline;
    private final String cmdLevelUsage;
    private final String cmdLevelNoData;
    private final String cmdLevelHeader;
    private final String cmdLevelNick;
    private final String cmdLevelLevelPoints;
    private final String cmdLevelPointsToNext;
    private final String cmdLevelFooter;
    
    private final ItemBuilder cobbleItem;
    private final ItemBuilder messagesItem;
    private final ItemBuilder turboItem;
    private final ItemBuilder levelItem;
    private final ItemBuilder enableItemAll;
    private final ItemBuilder disableItemAll;
    private final ItemBuilder fillerItem;
    private final ItemBuilder lockedItem;
    private final List<String> guiItemLores;
    private final List<String> guiLockedLores;
    private final List<String> guiCobbleLores;
    private final List<String> guiMessagesLores;
    private final List<String> guiTurboLores;
    private final List<String> enableAllLores;
    private final List<String> disableAllLores;
    private final List<String> guiLevelLores;
    private final List<Integer> chatLevels;
    private final int dbPort;
    private final int dbMaxPool;
    private final long dbConnectionTimeoutMs;
    private final long dbIdleTimeoutMs;
    private final long dbLeakDetectionThresholdMs;
    private final int guiSize;
    private final int cobbleSlot;
    private final int messagesSlot;
    private final int turboSlot;
    private final int levelSlot;
    private final int enableAllSlot;
    private final int disableAllSlot;
    private final int pointsToLvlUp;
    private final int maxLevel;
    private final boolean enchanted;
    private final boolean toInv;
    private final boolean messageInv;
    private final boolean cobbleStatus;
    private final boolean messagesStatus;
    private final boolean turboStatus;
    private final boolean levelStatus;
    private final boolean enableAllStatus;
    private final boolean disableAllStatus;
    private final boolean fillerStatus;
    private final boolean lvlStatus;
    private final boolean actionbarStatus;
    private long turboDrop;
    private long turboExp;
    private static ConfigManager configManager;
    private final DropPlugin plugin;

    public ConfigManager(DropPlugin plugin) {
        configManager = this;
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        FileConfiguration fc = this.plugin.getConfig();

        this.dbType = getStringSafe(fc, "database.type", "sqlite").toLowerCase();
        this.dbHost = getStringSafe(fc, "database.host", "localhost");
        this.dbUser = getStringSafe(fc, "database.user", "root");
        this.dbPass = getStringSafe(fc, "database.password", "");
        this.dbBase = getStringSafe(fc, "database.base", "minecraft");
        this.dbPort = getIntSafe(fc, "database.port", 3306);
        this.dbMaxPool = getIntSafe(fc, "database.maxPool", 10);
        this.dbConnectionTimeoutMs = getLongSafe(fc, "database.connectionTimeoutMs", 30000L);
        this.dbIdleTimeoutMs = getLongSafe(fc, "database.idleTimeoutMs", 600000L);
        this.dbLeakDetectionThresholdMs = getLongSafe(fc, "database.leakDetectionThresholdMs", 0L);

        this.dropMessage = getStringSafe(fc, "messages.drop", "");
        this.turboMessage = getStringSafe(fc, "messages.turbo.message", "");
        this.turboGetMessage = getStringSafe(fc, "messages.turbo.get", "");
        this.turboExp = getLongSafe(fc, "turbos.exp", 0L);
        this.turboDrop = getLongSafe(fc, "turbos.drop", 0L);
        this.guiName = getStringSafe(fc, "gui.name", "Drops");
        this.guiSize = getIntSafe(fc, "gui.size", 9);
        this.guiFillerName = getStringSafe(fc, "gui.filler.name", "");
        this.fillerStatus = getBooleanSafe(fc, "gui.filler.status", true);
        this.enchanted = getBooleanSafe(fc, "gui.item.enchanted", false);
        this.toInv = getBooleanSafe(fc, "settings.toinv.status", true);
        this.messageInv = getBooleanSafe(fc, "settings.toinv.message-status", true);
        this.messageInvFull = getStringSafe(fc, "settings.toinv.messageFullInv", "");
        this.actionbarStatus = getBooleanSafe(fc, "settings.actionbar.status", false);
        this.actionbarMessage = getStringSafe(fc, "settings.actionbar.message", "");
        
        // Load command messages
        this.cmdDropNoPermission = getStringSafe(fc, "messages.commands.drop.no-permission", "&4Blad: &cNie masz uprawnien do tej komendy! &7(dropplugin.cmd.drop)");
        this.cmdDropOnlyPlayers = getStringSafe(fc, "messages.commands.drop.only-players", "&4Blad: &cTa komenda moze byc uzywana tylko przez graczy.");
        
        this.cmdADropNoPermission = getStringSafe(fc, "messages.commands.adrop.no-permission", "&4Blad: &cNie masz uprawnien do tej komendy! &7(dropplugin.cmd.adrop)");
        this.cmdADropUsage = List.copyOf(getStringListSafe(fc, "messages.commands.adrop.usage"));
        this.cmdADropReloading = getStringSafe(fc, "messages.commands.adrop.reloading", "&7Przeladowywanie...");
        this.cmdADropReloaded = getStringSafe(fc, "messages.commands.adrop.reloaded", "&eDrop &7przeladowano! ({TIME}s)");
        this.cmdADropReloadError = getStringSafe(fc, "messages.commands.adrop.reload-error", "&4Blad: &cNie udalo sie przeladowac konfiguracji.");
        this.cmdADropInvalidTime = getStringSafe(fc, "messages.commands.adrop.invalid-time", "&4Blad: &cNiepoprawny czas: {TIME}");
        this.cmdADropTurboEnabled = getStringSafe(fc, "messages.commands.adrop.turbo-enabled", "&aWlaczyles {TYPE} na: {TIME}");
        this.cmdADropTurboEnabledFor = getStringSafe(fc, "messages.commands.adrop.turbo-enabled-for", "&aWlaczyles {TYPE} na: {TIME} dla: {PLAYER}");
        this.cmdADropPlayerOffline = getStringSafe(fc, "messages.commands.adrop.player-offline", "&4Blad: &cGracz jest offline!");
        this.cmdADropPlayerDataError = getStringSafe(fc, "messages.commands.adrop.player-data-error", "&4Blad: &cNie mozna zaladowac danych gracza!");
        this.cmdADropLevelMinMax = getStringSafe(fc, "messages.commands.adrop.level-min-max", "&4Blad: &cPoziom musi byc miedzy {MIN} a {MAX}!");
        this.cmdADropLevelNotNumber = getStringSafe(fc, "messages.commands.adrop.level-not-number", "&4Blad: &cPodany poziom nie jest liczba!");
        this.cmdADropPointsMinMax = getStringSafe(fc, "messages.commands.adrop.points-min-max", "&4Blad: &cPunkty musza byc miedzy {MIN} a {MAX}!");
        this.cmdADropPointsNotNumber = getStringSafe(fc, "messages.commands.adrop.points-not-number", "&4Blad: &cPodane punkty nie sa liczba!");
        this.cmdADropLevelChanged = getStringSafe(fc, "messages.commands.adrop.level-changed", "&aZmieniono poziom gracza &7{PLAYER} &az &7{OLD_LEVEL} &ana &7{NEW_LEVEL} &ai ustawiono punkty na &7{POINTS}");
        
        this.cmdLevelNoPermission = getStringSafe(fc, "messages.commands.level.no-permission", "&4Blad: &cNie masz uprawnien do tej komendy! &7(dropplugin.cmd.level)");
        this.cmdLevelNoALevelPermission = getStringSafe(fc, "messages.commands.level.no-alevel-permission", "&4Blad: &cNie masz uprawnien do sprawdzania innych graczy! &7(dropplugin.cmd.alevel)");
        this.cmdLevelOnlyPlayers = getStringSafe(fc, "messages.commands.level.only-players", "&4Blad: &cTa komenda moze byc uzyta tylko przez graczy.");
        this.cmdLevelPlayerDataError = getStringSafe(fc, "messages.commands.level.player-data-error", "&4Blad: &cNie mozna zaladowac Twoich danych!");
        this.cmdLevelTargetPlayerDataError = getStringSafe(fc, "messages.commands.level.target-player-data-error", "&4Blad: &cNie mozna zaladowac danych podanego gracza!");
        this.cmdLevelPlayerOffline = getStringSafe(fc, "messages.commands.level.player-offline", "&4Blad: &cPodany gracz nie jest online!");
        this.cmdLevelUsage = getStringSafe(fc, "messages.commands.level.usage", "&cPoprawne uzycie: &7/level [nick_gracza]");
        this.cmdLevelNoData = getStringSafe(fc, "messages.commands.level.no-data", "&4Blad: &cBrak danych gracza.");
        this.cmdLevelHeader = getStringSafe(fc, "messages.commands.level.header", "&8&m-----------------&8[ &f&lPOZIOM GRACZA &8]&8&m-----------------");
        this.cmdLevelNick = getStringSafe(fc, "messages.commands.level.nick", " &8* &7Nick: &e{PLAYER}");
        this.cmdLevelLevelPoints = getStringSafe(fc, "messages.commands.level.level-points", " &8* &7Aktualny poziom kopania: &a{LEVEL} &7, punkty: &6{POINTS}");
        this.cmdLevelPointsToNext = getStringSafe(fc, "messages.commands.level.points-to-next", " &8* &7Do nastepnego poziomu brakuje: &c{POINTS_TO_NEXT}");
        this.cmdLevelFooter = getStringSafe(fc, "messages.commands.level.footer", "&8&m-----------------&8[ &f&lPOZIOM GRACZA &8]&8&m-----------------");
        
        this.guiStatusOn = getStringSafe(fc, "gui.status.enable", "ON");
        this.guiStatusOff = getStringSafe(fc, "gui.status.disable", "OFF");
        this.guiItemName = getStringSafe(fc, "gui.item.name", "");
        this.guiItemLores = List.copyOf(getStringListSafe(fc, "gui.item.lores"));
        this.guiLockedName = getStringSafe(fc, "gui.locked.name", "&c&lZABLOKOWANY DROP");
        this.guiLockedLores = List.copyOf(getStringListSafe(fc, "gui.locked.lores"));
        this.guiCobbleName = getStringSafe(fc, "gui.cobble.name", "");
        this.cobbleStatus = getBooleanSafe(fc, "gui.cobble.status", true);
        this.guiCobbleLores = List.copyOf(getStringListSafe(fc, "gui.cobble.lores"));
        this.guiMessagesName = getStringSafe(fc, "gui.messages.name", "");
        this.messagesStatus = getBooleanSafe(fc, "gui.messages.status", true);
        this.guiMessagesLores = List.copyOf(getStringListSafe(fc, "gui.messages.lores"));
        this.guiTurboName = getStringSafe(fc, "gui.turbos.name", "");
        this.turboStatus = getBooleanSafe(fc, "gui.turbos.status", true);
        this.guiTurboLores = List.copyOf(getStringListSafe(fc, "gui.turbos.lores"));
        this.guiLevelName = getStringSafe(fc, "gui.level.name", "");
        this.levelStatus = getBooleanSafe(fc, "gui.level.status", true);
        this.guiLevelLores = List.copyOf(getStringListSafe(fc, "gui.level.lore"));
        this.enableAllName = getStringSafe(fc, "gui.enable-all.name", "");
        this.enableAllLores = List.copyOf(getStringListSafe(fc, "gui.enable-all.lores"));
        this.enableAllStatus = getBooleanSafe(fc, "gui.enable-all.status", true);
        this.disableAllName = getStringSafe(fc, "gui.disable-all.name", "");
        this.disableAllLores = List.copyOf(getStringListSafe(fc, "gui.disable-all.lores"));
        this.disableAllStatus = getBooleanSafe(fc, "gui.disable-all.status", true);
        this.cobbleSlot = getIntSafe(fc, "gui.cobble.slot", 10);
        this.messagesSlot = getIntSafe(fc, "gui.messages.slot", 11);
        this.turboSlot = getIntSafe(fc, "gui.turbos.slot", 12);
        this.enableAllSlot = getIntSafe(fc, "gui.enable-all.slot", 13);
        this.disableAllSlot = getIntSafe(fc, "gui.disable-all.slot", 14);
        this.levelSlot = getIntSafe(fc, "gui.level.slot", 15);
        this.lvlStatus = getBooleanSafe(fc, "settings.lvling.status", true);
        this.pointsToLvlUp = getIntSafe(fc, "settings.lvling.pointsToLvlup", 100);
        this.maxLevel = getIntSafe(fc, "settings.lvling.maxLevel", 100);
        this.chatLevels = List.copyOf(fc.getIntegerList("settings.lvling.chatLevels"));
        this.levelMessage = getStringSafe(fc, "settings.lvling.chatLevelMessage", "");
        this.maxLevelMessage = getStringSafe(fc, "settings.lvling.chatLevelMaxMessage", "");
        this.lvlUpMessage = getStringSafe(fc, "settings.lvling.chatLevelUpMessage", "");

        this.cobbleItem = createItemBuilderSafe(fc.getString("gui.cobble.item"), Material.COBBLESTONE);
        this.messagesItem = createItemBuilderSafe(fc.getString("gui.messages.item"), Material.PAPER);
        this.turboItem = createItemBuilderSafe(fc.getString("gui.turbos.item"), Material.GOLD_INGOT);
        this.levelItem = createItemBuilderSafe(fc.getString("gui.level.item"), Material.EXPERIENCE_BOTTLE);
        this.fillerItem = createItemBuilderSafe(fc.getString("gui.filler.item"), Material.GRAY_STAINED_GLASS_PANE);
        this.enableItemAll = createItemBuilderSafe(fc.getString("gui.enable-all.item"), Material.GREEN_CONCRETE);
        this.disableItemAll = createItemBuilderSafe(fc.getString("gui.disable-all.item"), Material.RED_CONCRETE);
        this.lockedItem = createItemBuilderSafe(fc.getString("gui.locked.item"), Material.MAGMA_CREAM);

        this.chances = new HashMap<>();
        
        // Sprawdź czy używamy nowego formatu (ConfigurationSection) czy starego (List<String>)
        if (fc.isConfigurationSection("settings.chances")) {
            // Nowy format: chances jako sekcja konfiguracyjna
            var chancesSection = fc.getConfigurationSection("settings.chances");
            if (chancesSection != null) {
                for (String key : chancesSection.getKeys(false)) {
                    var chanceSection = chancesSection.getConfigurationSection(key);
                    if (chanceSection != null) {
                        String perm = chanceSection.getString("permission");
                        Double additionalChance = chanceSection.getDouble("additionalchance", 0.0);
                        if (perm != null) {
                            this.chances.put(key, new Chance(key, perm, additionalChance));
                        }
                    }
                }
            }
        } else {
            // Stary format dla wstecznej kompatybilności: chances jako lista stringów
            for (String s : fc.getStringList("settings.chances")) {
                String[] split = s.split("@");
                if (split.length >= 2) {
                    try {
                        this.chances.put(split[0], new Chance(split[0], Double.parseDouble(split[1])));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
    }

    private ItemBuilder createItemBuilderSafe(String materialName, Material fallback) {
        Material mat = materialName == null ? null : Material.matchMaterial(materialName);
        if (mat == null) mat = fallback != null ? fallback : Material.STONE;
        try {
            return new ItemBuilder(mat, 1);
        } catch (IllegalArgumentException e) {
            return new ItemBuilder(Objects.requireNonNullElse(fallback, Material.STONE), 1);
        }
    }

    private String getStringSafe(FileConfiguration fc, String path, String def) {
        String val = fc.getString(path);
        return val == null ? def : val;
    }

    private int getIntSafe(FileConfiguration fc, String path, int def) {
        return fc.contains(path) ? fc.getInt(path) : def;
    }

    private long getLongSafe(FileConfiguration fc, String path, long def) {
        return fc.contains(path) ? fc.getLong(path) : def;
    }

    private boolean getBooleanSafe(FileConfiguration fc, String path, boolean def) {
        return fc.contains(path) ? fc.getBoolean(path) : def;
    }

    private List<String> getStringListSafe(FileConfiguration fc, String path) {
        List<String> list = fc.getStringList(path);
        return list == null ? List.of() : list;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public void save() {
        this.plugin.getConfig().set("turbos.drop", this.turboDrop);
        this.plugin.getConfig().set("turbos.exp", this.turboExp);
        this.plugin.saveConfig();
    }

    public void addTurboDrop(long turboDrop) {
        if (turboDrop > System.currentTimeMillis()) {
            this.turboDrop = turboDrop;
        } else {
            this.turboDrop = System.currentTimeMillis() + turboDrop;
        }
    }

    public void addTurboExp(long turboExp) {
        if (turboExp > System.currentTimeMillis()) {
            this.turboExp = turboExp;
        } else {
            this.turboExp = System.currentTimeMillis() + turboExp;
        }
    }

    public long getTurboDrop() { return this.turboDrop; }
    public long getTurboExp() { return this.turboExp; }
    public boolean isTurboDrop() { return this.getTurboDrop() > System.currentTimeMillis(); }
    public boolean isTurboExp() { return this.getTurboExp() > System.currentTimeMillis(); }

    public String getMaxLevelMessage() { return this.maxLevelMessage; }
    public List<Integer> getChatLevels() { return this.chatLevels; }
    public String getLevelMessage() { return this.levelMessage; }
    public int getMaxLevel() { return this.maxLevel; }
    public String getDbType() { return this.dbType; }
    public String getDbHost() { return this.dbHost; }
    public String getDbUser() { return this.dbUser; }
    public String getDbPass() { return this.dbPass; }
    public String getDbBase() { return this.dbBase; }
    public int getDbPort() { return this.dbPort; }
    public int getDbMaxPool() { return this.dbMaxPool; }
    public long getDbConnectionTimeoutMs() { return this.dbConnectionTimeoutMs; }
    public long getDbIdleTimeoutMs() { return this.dbIdleTimeoutMs; }
    public long getDbLeakDetectionThresholdMs() { return this.dbLeakDetectionThresholdMs; }
    public String getDropMessage() { return this.dropMessage; }
    public String getLvlUpMessage() { return this.lvlUpMessage; }
    public String getTurboGetMessage() { return this.turboGetMessage; }
    public String getTurboMessage() { return this.turboMessage; }
    public String getGuiFillerName() { return this.guiFillerName; }
    public String getGuiName() { return this.guiName; }
    public String getGuiStatusOn() { return this.guiStatusOn; }
    public String getGuiStatusOff() { return this.guiStatusOff; }
    public String getGuiItemName() { return this.guiItemName; }
    public String getGuiLockedName() { return this.guiLockedName; }
    public String getGuiCobbleName() { return this.guiCobbleName; }
    public String getGuiMessagesName() { return this.guiMessagesName; }
    public String getGuiTurboName() { return this.guiTurboName; }
    public String getGuiLevelName() { return this.guiLevelName; }
    public String getEnableAllName() { return this.enableAllName; }
    public String getDisableAllName() { return this.disableAllName; }
    public String getMessageInvFull() { return this.messageInvFull; }
    public String getActionbarMessage() { return this.actionbarMessage; }
    public ItemBuilder getCobbleItem() { return this.cobbleItem; }
    public ItemBuilder getMessagesItem() { return this.messagesItem; }
    public ItemBuilder getTurboItem() { return this.turboItem; }
    public ItemBuilder getLevelItem() { return this.levelItem; }
    public ItemBuilder getEnableItemAll() { return this.enableItemAll; }
    public ItemBuilder getDisableItemAll() { return this.disableItemAll; }
    public ItemBuilder getFillerItem() { return this.fillerItem; }
    public ItemBuilder getLockedItem() { return this.lockedItem; }
    public Map<String, Chance> getChances() { return Collections.unmodifiableMap(this.chances); }
    public List<String> getGuiItemLores() { return this.guiItemLores; }
    public List<String> getGuiLockedLores() { return this.guiLockedLores; }
    public List<String> getGuiCobbleLores() { return this.guiCobbleLores; }
    public List<String> getGuiMessagesLores() { return this.guiMessagesLores; }
    public List<String> getGuiTurboLores() { return this.guiTurboLores; }
    public List<String> getEnableAllLores() { return this.enableAllLores; }
    public List<String> getDisableAllLores() { return this.disableAllLores; }
    public List<String> getGuiLevelLores() { return this.guiLevelLores; }
    public int getGuiSize() { return this.guiSize; }
    public int getCobbleSlot() { return this.cobbleSlot; }
    public int getMessagesSlot() { return this.messagesSlot; }
    public int getTurboSlot() { return this.turboSlot; }
    public int getLevelSlot() { return this.levelSlot; }
    public int getEnableAllSlot() { return this.enableAllSlot; }
    public int getDisableAllSlot() { return this.disableAllSlot; }
    public int getPointsToLvlUp() { return this.pointsToLvlUp; }
    public boolean isEnchanted() { return this.enchanted; }
    public boolean isToInv() { return this.toInv; }
    public boolean isMessageInv() { return this.messageInv; }
    public boolean isCobbleStatus() { return this.cobbleStatus; }
    public boolean isMessagesStatus() { return this.messagesStatus; }
    public boolean isTurboStatus() { return this.turboStatus; }
    public boolean isLevelStatus() { return this.levelStatus; }
    public boolean isEnableAllStatus() { return this.enableAllStatus; }
    public boolean isDisableAllStatus() { return this.disableAllStatus; }
    public boolean isFillerStatus() { return this.fillerStatus; }
    public boolean isLvlStatus() { return this.lvlStatus; }
    public boolean isActionbarStatus() { return this.actionbarStatus; }
    
    // Command messages getters - DropCommand
    public String getCmdDropNoPermission() { return this.cmdDropNoPermission; }
    public String getCmdDropOnlyPlayers() { return this.cmdDropOnlyPlayers; }
    
    // Command messages getters - ADropCommand
    public String getCmdADropNoPermission() { return this.cmdADropNoPermission; }
    public List<String> getCmdADropUsage() { return this.cmdADropUsage; }
    public String getCmdADropReloading() { return this.cmdADropReloading; }
    public String getCmdADropReloaded() { return this.cmdADropReloaded; }
    public String getCmdADropReloadError() { return this.cmdADropReloadError; }
    public String getCmdADropInvalidTime() { return this.cmdADropInvalidTime; }
    public String getCmdADropTurboEnabled() { return this.cmdADropTurboEnabled; }
    public String getCmdADropTurboEnabledFor() { return this.cmdADropTurboEnabledFor; }
    public String getCmdADropPlayerOffline() { return this.cmdADropPlayerOffline; }
    public String getCmdADropPlayerDataError() { return this.cmdADropPlayerDataError; }
    public String getCmdADropLevelMinMax() { return this.cmdADropLevelMinMax; }
    public String getCmdADropLevelNotNumber() { return this.cmdADropLevelNotNumber; }
    public String getCmdADropPointsMinMax() { return this.cmdADropPointsMinMax; }
    public String getCmdADropPointsNotNumber() { return this.cmdADropPointsNotNumber; }
    public String getCmdADropLevelChanged() { return this.cmdADropLevelChanged; }
    
    // Command messages getters - LevelCommand
    public String getCmdLevelNoPermission() { return this.cmdLevelNoPermission; }
    public String getCmdLevelNoALevelPermission() { return this.cmdLevelNoALevelPermission; }
    public String getCmdLevelOnlyPlayers() { return this.cmdLevelOnlyPlayers; }
    public String getCmdLevelPlayerDataError() { return this.cmdLevelPlayerDataError; }
    public String getCmdLevelTargetPlayerDataError() { return this.cmdLevelTargetPlayerDataError; }
    public String getCmdLevelPlayerOffline() { return this.cmdLevelPlayerOffline; }
    public String getCmdLevelUsage() { return this.cmdLevelUsage; }
    public String getCmdLevelNoData() { return this.cmdLevelNoData; }
    public String getCmdLevelHeader() { return this.cmdLevelHeader; }
    public String getCmdLevelNick() { return this.cmdLevelNick; }
    public String getCmdLevelLevelPoints() { return this.cmdLevelLevelPoints; }
    public String getCmdLevelPointsToNext() { return this.cmdLevelPointsToNext; }
    public String getCmdLevelFooter() { return this.cmdLevelFooter; }
}