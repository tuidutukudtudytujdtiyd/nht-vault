package com.playervaultplus.data;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.vault.PlayerVault;
import com.playervaultplus.vault.VaultItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages vault data persistence to disk
 * Ensures safe serialization and deserialization of vault data
 */
public class VaultDataManager {

    private final PlayerVaultPlus plugin;
    private final Path vaultsDirectory;
    private final VaultDataHandler dataHandler;

    public VaultDataManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        this.vaultsDirectory = Paths.get(plugin.getDataFolder().getAbsolutePath(), "vaults");
        this.dataHandler = new VaultDataHandler();

        try {
            Files.createDirectories(vaultsDirectory);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create vaults directory!");
            e.printStackTrace();
        }
    }

    /**
     * Load vault for a player from disk
     */
    public PlayerVault loadVault(UUID playerUUID) {
        Path vaultFile = vaultsDirectory.resolve(playerUUID + ".vault");

        if (!Files.exists(vaultFile)) {
            return null;
        }

        try {
            return dataHandler.loadVault(vaultFile, playerUUID);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load vault for " + playerUUID + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save vault to disk
     * Thread-safe operation
     */
    public void saveVault(PlayerVault vault) {
        Path vaultFile = vaultsDirectory.resolve(vault.getPlayerUUID() + ".vault");

        try {
            dataHandler.saveVault(vault, vaultFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save vault for " + vault.getPlayerUUID() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all vaults from disk
     * Called on server startup
     */
    public void loadAllVaults() {
        try {
            File[] vaultFiles = vaultsDirectory.toFile().listFiles((dir, name) -> name.endsWith(".vault"));
            if (vaultFiles != null) {
                plugin.getLogger().info("Found " + vaultFiles.length + " vault files");
                for (File file : vaultFiles) {
                    try {
                        UUID uuid = UUID.fromString(file.getName().replace(".vault", ""));
                        // Vaults will be loaded on demand
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid vault file name: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load vaults directory: " + e.getMessage());
        }
    }

    /**
     * Save all dirty vaults to disk
     * Called before shutdown
     */
    public void saveAllVaults() {
        try {
            plugin.getVaultManager().saveAllDirtyVaults();
            plugin.getLogger().info("All vaults saved");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save all vaults: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get vault file for a player
     */
    public Path getVaultFile(UUID playerUUID) {
        return vaultsDirectory.resolve(playerUUID + ".vault");
    }
}
