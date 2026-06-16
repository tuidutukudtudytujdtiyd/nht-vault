package com.playervaultplus.hook;

import com.playervaultplus.PlayerVaultPlus;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * Hook for MMOItems plugin integration
 * Safely handles serialization and deserialization of MMOItems custom items
 * Prevents duplication and corruption issues
 */
public class MMOItemsHook {

    private final PlayerVaultPlus plugin;
    private static final String MMOITEMS_NBT_KEY = "MMOITEMS_ITEM_ID";

    public MMOItemsHook(PlayerVaultPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the MMOItems hook
     */
    public void initialize() {
        if (!isMMOItemsAvailable()) {
            throw new RuntimeException("MMOItems plugin not found or not properly loaded");
        }
        plugin.getLogger().info("MMOItems hook initialized successfully");
    }

    /**
     * Check if MMOItems is available
     */
    private boolean isMMOItemsAvailable() {
        try {
            Class.forName("net.Indyuce.mmoitems.MMOItems");
            return MMOItems.plugin != null && MMOItems.plugin.isEnabled();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if an ItemStack is an MMOItems item
     */
    public boolean isMMOItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        try {
            return MMOItems.getID(item) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get MMOItem ID and type
     */
    public String getMMOItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        try {
            return MMOItems.getID(item);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get MMOItem type
     */
    public String getMMOItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        try {
            return MMOItems.getType(item);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get MMOItem object
     */
    public MMOItem getMMOItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        try {
            String id = MMOItems.getID(item);
            String type = MMOItems.getType(item);
            
            if (id != null && type != null) {
                return MMOItems.getItemStack(type, id).getMMOItem();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get MMOItem: " + e.getMessage());
        }
        return null;
    }

    /**
     * Build MMOItem from scratch
     */
    public ItemStack buildMMOItem(String type, String id) {
        try {
            net.Indyuce.mmoitems.api.item.NBTItem nbtItem = MMOItems.getItemStack(type, id);
            return nbtItem != null ? nbtItem.toItem() : null;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to build MMOItem " + type + ":" + id + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Safe clone of MMOItem preserving all properties
     */
    public ItemStack cloneMMOItem(ItemStack original) {
        if (original == null || !isMMOItem(original)) {
            return original;
        }
        
        try {
            String type = getMMOItemType(original);
            String id = getMMOItemId(original);
            
            if (type != null && id != null) {
                return buildMMOItem(type, id);
            }
            // Fallback to normal clone
            return original.clone();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clone MMOItem: " + e.getMessage());
            return original.clone();
        }
    }

    /**
     * Get formatted item name (for logging/display)
     */
    public String getFormattedItemName(ItemStack item) {
        if (!isMMOItem(item)) {
            return null;
        }
        
        try {
            String type = getMMOItemType(item);
            String id = getMMOItemId(item);
            
            if (type != null && id != null) {
                ItemMeta meta = item.getItemMeta();
                String displayName = (meta != null && meta.hasDisplayName()) 
                    ? meta.getDisplayName() 
                    : item.getType().name();
                return String.format("%s [%s:%s]", displayName, type, id);
            }
        } catch (Exception e) {
            // Silent fail
        }
        return null;
    }

    /**
     * Verify MMOItem validity and data integrity
     */
    public boolean isValidMMOItem(ItemStack item) {
        if (!isMMOItem(item)) {
            return false;
        }
        
        try {
            String type = getMMOItemType(item);
            String id = getMMOItemId(item);
            
            if (type == null || id == null) {
                return false;
            }
            
            // Try to rebuild to verify it exists
            ItemStack rebuilt = buildMMOItem(type, id);
            return rebuilt != null && !rebuilt.getType().isAir();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Store MMOItem metadata for safe restoration
     */
    public MMOItemMetadata extractMetadata(ItemStack item) {
        if (!isMMOItem(item)) {
            return null;
        }
        
        try {
            String type = getMMOItemType(item);
            String id = getMMOItemId(item);
            int quantity = item.getAmount();
            
            ItemMeta meta = item.getItemMeta();
            String displayName = (meta != null && meta.hasDisplayName()) 
                ? meta.getDisplayName() 
                : null;
            
            return new MMOItemMetadata(type, id, quantity, displayName);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to extract MMOItem metadata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restore MMOItem from metadata
     */
    public ItemStack restoreFromMetadata(MMOItemMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        
        try {
            ItemStack item = buildMMOItem(metadata.getType(), metadata.getId());
            if (item != null) {
                item.setAmount(metadata.getQuantity());
                
                if (metadata.getDisplayName() != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(metadata.getDisplayName());
                        item.setItemMeta(meta);
                    }
                }
            }
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore MMOItem from metadata: " + e.getMessage());
            return null;
        }
    }

    /**
     * Container for MMOItem metadata
     */
    public static class MMOItemMetadata {
        private final String type;
        private final String id;
        private final int quantity;
        private final String displayName;

        public MMOItemMetadata(String type, String id, int quantity, String displayName) {
            this.type = type;
            this.id = id;
            this.quantity = Math.max(1, quantity);
            this.displayName = displayName;
        }

        public String getType() { return type; }
        public String getId() { return id; }
        public int getQuantity() { return quantity; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return String.format("%s:%s (qty:%d)", type, id, quantity);
        }
    }
}
