package com.playervaultplus.data;

import com.playervaultplus.vault.PlayerVault;
import com.playervaultplus.vault.VaultItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles serialization and deserialization of vault data
 * Uses JSON-like format for readability and compatibility
 */
public class VaultDataHandler {

    /**
     * Load vault from file
     */
    public PlayerVault loadVault(Path file, UUID playerUUID) throws IOException {
        PlayerVault vault = new PlayerVault(playerUUID);
        Map<Integer, VaultItem> items = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse line format: slot:quantity:serializedData
                String[] parts = line.split(":", 3);
                if (parts.length >= 3) {
                    try {
                        int slot = Integer.parseInt(parts[0]);
                        int quantity = Integer.parseInt(parts[1]);
                        String serializedData = parts[2];

                        if (slot >= 0 && slot < 300 && quantity > 0 && !serializedData.isEmpty()) {
                            VaultItem item = new VaultItem(serializedData, quantity);
                            items.put(slot, item);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                    }
                }
            }
        }

        // Set items in vault
        for (Map.Entry<Integer, VaultItem> entry : items.entrySet()) {
            VaultItem item = entry.getValue();
            org.bukkit.inventory.ItemStack itemStack = item.toItemStack();
            if (itemStack != null) {
                vault.addItem(entry.getKey(), itemStack);
            }
        }

        vault.setClean();
        return vault;
    }

    /**
     * Save vault to file
     * Uses atomic write to prevent data corruption
     */
    public void saveVault(PlayerVault vault, Path file) throws IOException {
        // Create temporary file
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()))) {
            // Write header
            writer.write("# PlayerVaultPlus Vault Data\n");
            writer.write("# UUID: " + vault.getPlayerUUID() + "\n");
            writer.write("# Format: slot:quantity:serializedData\n");
            writer.newLine();

            // Write items
            Map<Integer, VaultItem> items = vault.getAllItems();
            for (Map.Entry<Integer, VaultItem> entry : items.entrySet()) {
                VaultItem item = entry.getValue();
                if (item != null && !item.isEmpty()) {
                    writer.write(entry.getKey() + ":" + item.getQuantity() + ":" + item.getSerializedData());
                    writer.newLine();
                }
            }
        }

        // Atomic replace
        Files.move(tempFile, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
