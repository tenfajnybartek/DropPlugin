package pl.tenfajnybartek.dropplugin.managers;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.tenfajnybartek.dropplugin.base.DropPlugin;

import java.io.File;
import java.io.IOException;

public class DropConfigManager {
    private final FileConfiguration dropConfig;
    private final File file;

    public DropConfigManager(DropPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "drops.yml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            plugin.saveResource("drops.yml", false);
        }

        FileConfiguration cfg = new YamlConfiguration();
        try {
            cfg = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            try {
                ((YamlConfiguration) cfg).load(file);
            } catch (IOException | InvalidConfigurationException ex) {
                ex.printStackTrace();
            }
        }
        this.dropConfig = cfg;
    }

    public FileConfiguration getDropConfig() {
        return this.dropConfig;
    }

    public void reload() {
        try {
            ((YamlConfiguration) this.dropConfig).load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.dropConfig.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}