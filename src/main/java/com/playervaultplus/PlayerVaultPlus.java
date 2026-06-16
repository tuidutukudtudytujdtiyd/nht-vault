package com.playervaultplus;

import com.playervaultplus.command.VaultCommand;
import com.playervaultplus.config.ConfigManager;
import com.playervaultplus.database.DatabaseConnector;
import com.playervaultplus.database.DatabaseManager;
import com.playervaultplus.gui.GUIManager;
import com.playervaultplus.hook.HookManager;
import com.playervaultplus.listener.InventoryListener;
import com.playervaultplus.serialization.SafeItemSerializer;
import com.playervaultplus.vault.VaultItem;
import com.playervaultplus.vault.VaultManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for PlayerVaultPlus
 * Handles initialization and lifecycle management
 * Version 1.2.0 - Added MMOItems Hook + Safe Serialization
 */
public class PlayerVaultPlus extends JavaPlugin {

    private static PlayerVaultPlus instance;
    private ConfigManager configManager;
    private DatabaseConnector databaseConnector;
    private DatabaseManager databaseManager;
    private VaultManager vaultManager;
    private GUIManager guiManager;
    private HookManager hookManager;
    private SafeItemSerializer itemSerializer;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§6═════════════════════════════════════");
        getLogger().info("§aPlayerVaultPlus v" + getDescription().getVersion() + " is starting up...");
        getLogger().info("§6═════════════════════════════════════");

        try {
            // Initialize config manager
            this.configManager = new ConfigManager(this);
            getLogger().info("✓ Config manager initialized");

            // Initialize database
            this.databaseConnector = new DatabaseConnector(this, configManager);
            databaseConnector.initialize();
            getLogger().info("✓ Database connector initialized");

            // Check database connection
            if (!databaseConnector.testConnection()) {
                throw new Exception("Failed to connect to MySQL database!");
            }
            getLogger().info("✓ Database connection successful");

            // Initialize database manager
            this.databaseManager = new DatabaseManager(this, databaseConnector, configManager);
            databaseManager.initialize();
            getLogger().info("✓ Database tables initialized");

            // Initialize vault manager
            this.vaultManager = new VaultManager(this);
            getLogger().info("✓ Vault manager initialized");

            // Initialize GUI manager
            this.guiManager = new GUIManager(this);
            getLogger().info("✓ GUI manager initialized");

            // Initialize hook manager
            this.hookManager = new HookManager(this);
            hookManager.loadHooks();
            getLogger().info("✓ Hook manager initialized");

            // Initialize safe item serializer
            this.itemSerializer = new SafeItemSerializer(this, hookManager.getMMOItemsHook());
            VaultItem.initialize(itemSerializer, hookManager.getMMOItemsHook());
            getLogger().info("✓ Item serializer initialized");

            // Register commands
            new VaultCommand(this);
            getLogger().info("✓ Commands registered");

            // Register listeners
            new InventoryListener(this);
            getLogger().info("✓ Event listeners registered");

            getLogger().info("§a✓ PlayerVaultPlus v" + getDescription().getVersion() + " has been enabled!");
            getLogger().info("§6═════════════════════════════════════");
        } catch (Exception e) {
            getLogger().severe("✗ Failed to enable PlayerVaultPlus!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Save all vaults
            if (vaultManager != null) {
                vaultManager.saveAllDirtyVaults();
                getLogger().info("✓ All vaults saved");
            }

            // Close database connection
            if (databaseConnector != null) {
                databaseConnector.close();
                getLogger().info("✓ Database connection closed");
            }

            getLogger().info("✓ PlayerVaultPlus has been disabled!");
        } catch (Exception e) {
            getLogger().severe("✗ Error during shutdown!");
            e.printStackTrace();
        }
    }

    // Static accessors
    public static PlayerVaultPlus getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public SafeItemSerializer getItemSerializer() {
        return itemSerializer;
    }
}
