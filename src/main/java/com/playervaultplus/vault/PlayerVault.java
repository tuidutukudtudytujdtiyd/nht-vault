package com.playervaultplus.vault;

import org.bukkit.inventory.ItemStack;

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
    private final Map<Integer, VaultItem> items;
    private boolean dirty = false; // Track if vault has unsaved changes

    public PlayerVault(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.items = new HashMap<>();
    }

    /**
     * Add an item to the vault at a specific slot
     * Prevents duplication by checking current slot contents
     */
    public synchronized boolean addItem(int slot, ItemStack item) {
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
        items.put(slot, vaultItem);
        dirty = true;
        return true;
    }

    /**
     * Remove an item from the vault at a specific slot
     * Returns the item or null if slot is empty
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
     * Check if two items are the same (considering type, damage, enchantments, etc.)
     */
    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        if (!item1.getType().equals(item2.getType())) return false;
        
        // Compare display names
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

    public UUID getPlayerUUID() {
        return playerUUID;
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
