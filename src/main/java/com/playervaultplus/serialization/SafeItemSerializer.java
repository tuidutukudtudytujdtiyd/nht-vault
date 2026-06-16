package com.playervaultplus.serialization;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.hook.MMOItemsHook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Safe serialization handler for items including MMOItems
 * Prevents corruption and loss of custom item data
 */
public class SafeItemSerializer {

    private final PlayerVaultPlus plugin;
    private final MMOItemsHook mmoItemsHook;

    public SafeItemSerializer(PlayerVaultPlus plugin, MMOItemsHook mmoItemsHook) {
        this.plugin = plugin;
        this.mmoItemsHook = mmoItemsHook;
    }

    /**
     * Serialize ItemStack to Base64 string
     * Handles both regular and MMOItems
     */
    public String serialize(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        try {
            // For MMOItems, use metadata approach for extra safety
            if (mmoItemsHook != null && mmoItemsHook.isMMOItem(item)) {
                return serializeMMOItem(item);
            }
            
            // Regular item serialization
            return serializeRegularItem(item);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            e.printStackTrace();
            // Fallback to regular serialization
            try {
                return serializeRegularItem(item);
            } catch (IOException ex) {
                return null;
            }
        }
    }

    /**
     * Serialize regular Bukkit ItemStack
     */
    private String serializeRegularItem(ItemStack item) throws IOException {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)
        ) {
            boos.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Serialize MMOItem with metadata backup
     */
    private String serializeMMOItem(ItemStack item) throws IOException {
        // First, use standard serialization
        String standardSerialized = serializeRegularItem(item);
        
        // Also extract metadata for recovery
        MMOItemsHook.MMOItemMetadata metadata = mmoItemsHook.extractMetadata(item);
        
        if (metadata != null) {
            // Store with metadata marker: MMOITEM:[type:id:qty:displayName]:[standardSerialized]
            String metadataStr = String.format("MMOITEM:%s:%s:%d:%s:%s",
                metadata.getType(),
                metadata.getId(),
                metadata.getQuantity(),
                metadata.getDisplayName() != null ? Base64.getEncoder().encodeToString(metadata.getDisplayName().getBytes()) : "",
                standardSerialized
            );
            return metadataStr;
        }
        
        return standardSerialized;
    }

    /**
     * Deserialize Base64 string back to ItemStack
     * Handles both regular and MMOItems
     */
    public ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            // Check if it's MMOItem metadata
            if (data.startsWith("MMOITEM:")) {
                return deserializeMMOItem(data);
            }
            
            // Regular item deserialization
            return deserializeRegularItem(data);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
            e.printStackTrace();
            // Fallback
            try {
                return deserializeRegularItem(data);
            } catch (IOException ex) {
                return null;
            }
        }
    }

    /**
     * Deserialize regular Bukkit ItemStack
     */
    private ItemStack deserializeRegularItem(String data) throws IOException {
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)
        ) {
            return (ItemStack) bois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found during deserialization", e);
        }
    }

    /**
     * Deserialize MMOItem from metadata
     */
    private ItemStack deserializeMMOItem(String data) throws Exception {
        if (mmoItemsHook == null || !mmoItemsHook.isMMOItemsEnabled()) {
            // MMOItems not available, try regular deserialization of embedded data
            String[] parts = data.split(":", 6);
            if (parts.length >= 6) {
                return deserializeRegularItem(parts[5]);
            }
            throw new Exception("Cannot deserialize MMOItem without MMOItems hook");
        }

        try {
            String[] parts = data.split(":", 6);
            if (parts.length < 5) {
                throw new Exception("Invalid MMOItem metadata format");
            }

            String type = parts[1];
            String id = parts[2];
            int quantity = Integer.parseInt(parts[3]);
            String displayNameEncoded = parts[4];
            String standardSerialized = parts.length > 5 ? parts[5] : null;

            // Try to rebuild from MMOItems first
            ItemStack item = mmoItemsHook.buildMMOItem(type, id);
            
            if (item != null && !item.getType().isAir()) {
                item.setAmount(Math.max(1, quantity));
                
                if (!displayNameEncoded.isEmpty()) {
                    try {
                        String displayName = new String(Base64.getDecoder().decode(displayNameEncoded));
                        var meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(displayName);
                            item.setItemMeta(meta);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to restore display name: " + e.getMessage());
                    }
                }
                
                return item;
            }

            // Fallback to standard serialization if available
            if (standardSerialized != null && !standardSerialized.isEmpty()) {
                plugin.getLogger().warning("MMOItem " + type + ":" + id + " not found, attempting standard deserialization fallback");
                return deserializeRegularItem(standardSerialized);
            }

            throw new Exception("Failed to deserialize MMOItem: " + type + ":" + id);
        } catch (Exception e) {
            plugin.getLogger().warning("MMOItem deserialization failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Validate serialized data
     */
    public boolean isValidSerialized(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        try {
            if (data.startsWith("MMOITEM:")) {
                // Try to parse MMOItem metadata
                String[] parts = data.split(":", 6);
                return parts.length >= 5;
            }
            
            // Try to decode base64
            Base64.getDecoder().decode(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
