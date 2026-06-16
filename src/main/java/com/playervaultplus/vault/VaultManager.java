package com.playervaultplus.vault;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.database.DatabaseManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all player vaults with caching and database persistence
 * Ensures only one vault instance per player
 */
public class VaultManager {

    private final PlayerVaultPlus plugin;
    private final Map<UUID, PlayerVault> vaults = new HashMap<>();
    private final DatabaseManager databaseManager;

    public VaultManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    /**
     * Get or create a vault for a player
     */
    public synchronized PlayerVault getOrCreateVault(UUID playerUUID, String playerName) {
        return vaults.computeIfAbsent(playerUUID, uuid -> {
            try {
                // Try to load from database
                PlayerVault vault = databaseManager.loadVault(uuid, playerName);
                return vault;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load vault for " + playerUUID + ": " + e.getMessage());
                e.printStackTrace();
                return new PlayerVault(uuid);
            }
        });
    }

    /**
     * Get vault if it exists in cache
     */
    public synchronized PlayerVault getVault(UUID playerUUID) {
        return vaults.get(playerUUID);
    }

    /**
     * Save vault to database
     */
    public void saveVault(UUID playerUUID) {
        PlayerVault vault = vaults.get(playerUUID);
        if (vault != null && vault.isDirty()) {
            try {
                databaseManager.saveVault(vault);
                vault.setClean();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save vault: " + e.getMessage());
            }
        }
    }

    /**
     * Unload vault from cache
     */
    public synchronized void unloadVault(UUID playerUUID) {
        PlayerVault vault = vaults.remove(playerUUID);
        if (vault != null && vault.isDirty()) {
            try {
                databaseManager.saveVault(vault);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save vault on unload: " + e.getMessage());
            }
        }
    }

    /**
     * Save all dirty vaults to database
     */
    public synchronized void saveAllDirtyVaults() {
        vaults.values().stream()
            .filter(PlayerVault::isDirty)
            .forEach(vault -> {
                try {
                    databaseManager.saveVault(vault);
                    vault.setClean();
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to save vault: " + e.getMessage());
                }
            });
    }

    /**
     * Get number of cached vaults
     */
    public synchronized int getCachedVaultCount() {
        return vaults.size();
    }
}
