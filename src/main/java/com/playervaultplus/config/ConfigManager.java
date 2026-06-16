package com.playervaultplus.config;

import com.playervaultplus.PlayerVaultPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Manages plugin configuration loading and caching
 * Supports hot-reload of configuration
 */
public class ConfigManager {

    private final PlayerVaultPlus plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Load or create default config
     */
    public void loadConfig() {
        try {
            // Create plugins folder if not exists
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            configFile = new File(dataFolder, "config.yml");

            // Create default config if not exists
            if (!configFile.exists()) {
                try (InputStream input = plugin.getResource("config.yml")) {
                    if (input != null) {
                        Files.copy(input, configFile.toPath());
                        plugin.getLogger().info("Default config created");
                    }
                }
            }

            // Load config
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Config loaded successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config: " + e.getMessage());
            e.printStackTrace()
        }
    }

    /**
     * Reload config from file
     */
    public void reloadConfig() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Config reloaded successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
        }
    }

    /**
     * Get string config value with color code support
     */
    public String getString(String path, String def) {
        String value = config.getString(path, def);
        return value != null ? value : def;
    }

    /**
     * Get int config value
     */
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    /**
     * Get boolean config value
     */
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    /**
     * Get long config value
     */
    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    /**
     * Get vault title with color codes
     */
    public String getVaultTitle() {
        return getString("vault.title", "§6Vault §f(%page%/%total%)");
    }

    /**
     * Get vault size
     */
    public int getVaultSize() {
        return getInt("vault.size", 300);
    }

    /**
     * Get database type (mysql, sqlite)
     */
    public String getDatabaseType() {
        return getString("database.type", "mysql");
    }

    /**
     * Get MySQL host
     */
    public String getMySQLHost() {
        return getString("database.mysql.host", "localhost");
    }

    /**
     * Get MySQL port
     */
    public int getMySQLPort() {
        return getInt("database.mysql.port", 3306);
    }

    /**
     * Get MySQL username
     */
    public String getMySQLUsername() {
        return getString("database.mysql.username", "root");
    }

    /**
     * Get MySQL password
     */
    public String getMySQLPassword() {
        return getString("database.mysql.password", "password");
    }

    /**
     * Get MySQL database name
     */
    public String getMySQLDatabase() {
        return getString("database.mysql.database", "playervaultplus");
    }

    /**
     * Get button names from config
     */
    public String getButtonName(String buttonType) {
        return getString("buttons." + buttonType, buttonType);
    }

    /**
     * Get color code from config
     */
    public String getColor(String colorType) {
        return getString("colors." + colorType, "§f");
    }

    /**
     * Get message from config
     */
    public String getMessage(String messageKey) {
        return getString("messages." + messageKey, messageKey);
    }

    /**
     * Get auto-sort config
     */
    public boolean isAutoSortEnabled() {
        return getBoolean("auto-sort.enabled", true);
    }

    /**
     * Get auto-sort method
     */
    public String getAutoSortMethod() {
        return getString("auto-sort.sort-by", "category");
    }

    /**
     * Check if compress stacks is enabled
     */
    public boolean shouldCompressStacks() {
        return getBoolean("auto-sort.compress-stacks", true);
    }

    /**
     * Get backup interval in minutes
     */
    public long getBackupInterval() {
        return getLong("backup.interval", 1440); // 24 hours
    }

    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return getBoolean("advanced.debug", false);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
