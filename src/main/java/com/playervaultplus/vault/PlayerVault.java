package com.playervaultplus.vault;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's personal vault with thread-safe operations
 * Stores up to 300 items with serialization support
 */
public class PlayerVault {

    private static final int VAULT_SIZE = 300;
    private final UUID playerUUID;
    private int vaultId = -1;  // Database ID
    private final Map<Integer, VaultItem> items;
    private boolean dirty = false;

    public PlayerVault(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.items = new HashMap<>();
    }

    /**
     * Add an item to the vault at a specific slot
     * Prevents duplication by checking current slot contents
     */
    public synchronized boolean addItem(int slot, ItemStack item) {
        return addItem(slot, item, null, null);
    }

    /**
     * Add an item with custom display name and lore
     */
    public synchronized boolean addItem(int slot, ItemStack item, String displayName, String lore) {
        if (slot < 0 || slot >= VAULT_SIZE) {
            return false;
        }

        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }

        // Prevent duplication - ensure slot is empty or has same item
        VaultItem existing = items.get(slot);
        if (existing != null && !existing.isEmpty()) {
            ItemStack existingItem = existing.toItemStack();
            if (!isSameItem(item, existingItem)) {
                return false; // Cannot overwrite different item
            }
        }

        VaultItem vaultItem = new VaultItem(item.clone());
        vaultItem.setDisplayName(displayName);
        vaultItem.setLore(lore);
        items.put(slot, vaultItem);
        dirty = true;
        return true;
    }

    /**
     * Remove an item from the vault at a specific slot
     */
    public synchronized ItemStack removeItem(int slot) {
        if (slot < 0 || slot >= VAULT_SIZE) {
            return null;
        }

        VaultItem vaultItem = items.remove(slot);
        if (vaultItem != null && !vaultItem.isEmpty()) {
            dirty = true;
            return vaultItem.toItemStack();
        }
        return null;
    }

    /**
     * Get item at specific slot without removing it
     */
    public synchronized ItemStack getItem(int slot) {
        if (slot < 0 || slot >= VAULT_SIZE) {
            return null;
        }

        VaultItem vaultItem = items.get(slot);
        if (vaultItem != null && !vaultItem.isEmpty()) {
            return vaultItem.toItemStack();
        }
        return null;
    }

    /**
     * Get vault item with metadata
     */
    public synchronized VaultItem getVaultItem(int slot) {
        if (slot < 0 || slot >= VAULT_SIZE) {
            return null;
        }
        return items.get(slot);
    }

    /**
     * Check if two items are the same
     */
    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (!item1.getType().equals(item2.getType())) return false;
        
        String name1 = item1.getItemMeta() != null ? item1.getItemMeta().getDisplayName() : null;
        String name2 = item2.getItemMeta() != null ? item2.getItemMeta().getDisplayName() : null;
        
        return (name1 == null && name2 == null) || (name1 != null && name1.equals(name2));
    }

    /**
     * Clear a specific slot
     */
    public synchronized void clearSlot(int slot) {
        if (slot >= 0 && slot < VAULT_SIZE) {
            items.remove(slot);
            dirty = true;
        }
    }

    /**
     * Clear entire vault
     */
    public synchronized void clear() {
        items.clear();
        dirty = true;
    }

    /**
     * Get all items in vault
     */
    public synchronized Map<Integer, VaultItem> getAllItems() {
        return new HashMap<>(items);
    }

    /**
     * Check if vault is empty
     */
    public synchronized boolean isEmpty() {
        return items.isEmpty() || items.values().stream().allMatch(VaultItem::isEmpty);
    }

    /**
     * Get number of non-empty slots
     */
    public synchronized int getUsedSlots() {
        return (int) items.values().stream()
            .filter(item -> !item.isEmpty())
            .count();
    }

    /**
     * Get remaining empty slots
     */
    public synchronized int getEmptySlots() {
        return VAULT_SIZE - getUsedSlots();
    }

    /**
     * Auto-sort vault items by category
     */
    public synchronized void autoSort() {
        // Create sorted map
        Map<Integer, VaultItem> sortedItems = new HashMap<>();
        int targetSlot = 0;

        // Sort by item type
        items.values().stream()
            .filter(item -> !item.isEmpty())
            .sorted((a, b) -> a.getItemType().compareTo(b.getItemType()))
            .forEach(item -> {
                while (targetSlot < VAULT_SIZE && sortedItems.containsKey(targetSlot)) {
                    targetSlot++;
                }
                if (targetSlot < VAULT_SIZE) {
                    sortedItems.put(targetSlot, item);
                    targetSlot++;
                }
            });

        items.clear();
        items.putAll(sortedItems);
        dirty = true;
    }

    // Getters & Setters
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getVaultId() {
        return vaultId;
    }

    public void setVaultId(int vaultId) {
        this.vaultId = vaultId;
    }

    public int getVaultSize() {
        return VAULT_SIZE;
    }

    public synchronized boolean isDirty() {
        return dirty;
    }

    public synchronized void setClean() {
        dirty = false;
    }
}
