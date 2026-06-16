package com.playervaultplus.vault;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.hook.MMOItemsHook;
import com.playervaultplus.serialization.SafeItemSerializer;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item stored in the vault with serialization support and custom metadata
 * Safely handles both regular and MMOItems
 */
public class VaultItem {

    private String serializedData;
    private int quantity;
    private String displayName;  // Custom name
    private String lore;         // Custom lore
    private String itemType;     // Material/type name for quick lookup
    private boolean isMMOItem = false;  // Flag for MMOItems

    private static SafeItemSerializer serializer;
    private static MMOItemsHook mmoItemsHook;

    /**
     * Static initializer for serializer
     */
    public static void initialize(SafeItemSerializer serializer, MMOItemsHook mmoItemsHook) {
        VaultItem.serializer = serializer;
        VaultItem.mmoItemsHook = mmoItemsHook;
    }

    public VaultItem(ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            this.quantity = Math.max(1, item.getAmount());
            this.itemType = item.getType().name();
            
            // Check if it's MMOItem
            if (mmoItemsHook != null && mmoItemsHook.isMMOItem(item)) {
                this.isMMOItem = true;
            }
            
            // Serialize using safe serializer
            if (serializer != null) {
                this.serializedData = serializer.serialize(item);
            } else {
                this.serializedData = serializeItemFallback(item);
            }
            
            // Extract display name from item meta
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                this.displayName = item.getItemMeta().getDisplayName();
            }
        } else {
            this.quantity = 0;
            this.serializedData = null;
        }
    }

    public VaultItem(String serializedData, int quantity) {
        this.serializedData = serializedData;
        this.quantity = Math.max(1, quantity);
    }

    /**
     * Fallback serialization method
     */
    private static String serializeItemFallback(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        try {
            org.bukkit.util.io.BukkitObjectOutputStream boos = 
                new org.bukkit.util.io.BukkitObjectOutputStream(
                    new java.io.ByteArrayOutputStream());
            boos.writeObject(item);
            java.io.ByteArrayOutputStream baos = (java.io.ByteArrayOutputStream) boos.getClass().getDeclaredField("out").get(boos);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Serialize ItemStack to Base64 string for storage
     */
    public static String serializeItem(ItemStack item) {
        if (serializer != null) {
            return serializer.serialize(item);
        }
        return serializeItemFallback(item);
    }

    /**
     * Deserialize Base64 string back to ItemStack
     */
    public static ItemStack deserializeItem(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        
        if (serializer != null) {
            return serializer.deserialize(data);
        }
        
        try {
            java.io.ByteArrayInputStream bais = 
                new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(data));
            org.bukkit.util.io.BukkitObjectInputStream bois = 
                new org.bukkit.util.io.BukkitObjectInputStream(bais);
            return (ItemStack) bois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack toItemStack() {
        if (serializedData == null || serializedData.isEmpty()) {
            return null;
        }
        ItemStack item = deserializeItem(serializedData);
        if (item != null) {
            item.setAmount(Math.max(1, quantity));
        }
        return item;
    }

    public String getSerializedData() {
        return serializedData;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public String getItemType() {
        if (itemType == null && serializedData != null) {
            ItemStack item = toItemStack();
            if (item != null) {
                itemType = item.getType().name();
            }
        }
        return itemType != null ? itemType : "UNKNOWN";
    }

    public boolean isMMOItem() {
        return isMMOItem;
    }

    public boolean isEmpty() {
        return serializedData == null || serializedData.isEmpty() || quantity <= 0;
    }
}
