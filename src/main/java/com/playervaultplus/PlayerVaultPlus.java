package com.playervaultplus;

import com.playervaultplus.command.VaultCommand;
import com.playervaultplus.data.VaultDataManager;
import com.playervaultplus.gui.GUIManager;
import com.playervaultplus.listener.InventoryListener;
import com.playervaultplus.vault.VaultManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for PlayerVaultPlus
 * Handles initialization and lifecycle management
 */
public class PlayerVaultPlus extends JavaPlugin {

    private static PlayerVaultPlus instance;
    private VaultManager vaultManager;
    private GUIManager guiManager;
    private VaultDataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("PlayerVaultPlus is starting up...");

        try {
            // Initialize data manager first
            this.dataManager = new VaultDataManager(this);
            this.dataManager.loadAllVaults();
            getLogger().info("Data manager initialized");

            // Initialize vault manager
            this.vaultManager = new VaultManager(this);
            getLogger().info("Vault manager initialized");

            // Initialize GUI manager
            this.guiManager = new GUIManager(this);
            getLogger().info("GUI manager initialized");

            // Register commands
            new VaultCommand(this);
            getLogger().info("Commands registered");

            // Register listeners
            new InventoryListener(this);
            getLogger().info("Event listeners registered");

            getLogger().info("PlayerVaultPlus v" + getDescription().getVersion() + " has been enabled!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable PlayerVaultPlus!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Save all vaults before shutdown
            if (dataManager != null) {
                dataManager.saveAllVaults();
            }
            getLogger().info("PlayerVaultPlus has been disabled!");
        } catch (Exception e) {
            getLogger().severe("Error during shutdown!");
            e.printStackTrace();
        }
    }

    public static PlayerVaultPlus getInstance() {
        return instance;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public VaultDataManager getDataManager() {
        return dataManager;
    }
}
