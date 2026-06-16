package com.playervaultplus.vault;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.data.VaultDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all player vaults with caching
 * Ensures only one vault instance per player
 */
public class VaultManager {

    private final PlayerVaultPlus plugin;
    private final Map<UUID, PlayerVault> vaults = new HashMap<>();
    private final VaultDataManager dataManager;

    public VaultManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    /**
     * Get or create a vault for a player
     */
    public synchronized PlayerVault getOrCreateVault(UUID playerUUID) {
        return vaults.computeIfAbsent(playerUUID, uuid -> {
            // Try to load from disk first
            PlayerVault vault = dataManager.loadVault(uuid);
            if (vault == null) {
                // Create new vault if doesn't exist
                vault = new PlayerVault(uuid);
                dataManager.saveVault(vault);
            }
            return vault;
        });
    }

    /**
     * Get vault if it exists in cache
     */
    public synchronized PlayerVault getVault(UUID playerUUID) {
        return vaults.get(playerUUID);
    }

    /**
     * Save vault to disk
     */
    public void saveVault(UUID playerUUID) {
        PlayerVault vault = vaults.get(playerUUID);
        if (vault != null && vault.isDirty()) {
            dataManager.saveVault(vault);
            vault.setClean();
        }
    }

    /**
     * Unload vault from cache (called when player disconnects)
     */
    public synchronized void unloadVault(UUID playerUUID) {
        PlayerVault vault = vaults.remove(playerUUID);
        if (vault != null && vault.isDirty()) {
            dataManager.saveVault(vault);
        }
    }

    /**
     * Save all dirty vaults to disk
     */
    public synchronized void saveAllDirtyVaults() {
        vaults.values().stream()
            .filter(PlayerVault::isDirty)
            .forEach(vault -> {
                dataManager.saveVault(vault);
                vault.setClean();
            });
    }

    /**
     * Get number of cached vaults
     */
    public synchronized int getCachedVaultCount() {
        return vaults.size();
    }
}
