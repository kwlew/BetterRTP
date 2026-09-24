package dev.kwlew.managers;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration loaded = plugin.getConfig();

        try (InputStream bundled = plugin.getResource("config.yml")) {
            if (bundled == null) {
                plugin.getLogger().severe("Bundled config.yml is missing from the jar; "
                        + "falling back to built-in values for anything absent on disk.");
            }
            else {
                loaded.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(bundled, StandardCharsets.UTF_8)
                ));
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not load config.yml", ex);
        }

        this.config = loaded;
    }

    private boolean defined(String path) {
        if (config.isSet(path)) {
            return true;
        }

        Configuration defaults = config.getDefaults();

        return defaults != null && defaults.isSet(path);
    }

    private int intAt(String path, int fallback) {
        return defined(path) ? config.getInt(path) : fallback;
    }

    private boolean boolAt(String path) {
        return !defined(path) || config.getBoolean(path);
    }

    private double doubleAt(String path) {
        return defined(path) ? config.getDouble(path) : 1.0;
    }

    private String stringAt(String path, String fallback) {
        String value = defined(path) ? config.getString(path) : null;

        return value != null ? value : fallback;
    }
}
