package com.playervaultplus.pagination;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages pagination for vault display
 * Converts 300 slots into manageable pages for chest GUI (27 slots max)
 */
public class PageManager {

    private static final int INVENTORY_ROWS = 5; // 5 rows for items + 1 row for controls = 6 rows total (54 slots)
    private static final int ITEMS_PER_PAGE = INVENTORY_ROWS * 9; // 45 items per page
    private static final int VAULT_SIZE = 300;

    private int currentPage = 0;
    private List<Integer> vaultSlots = new ArrayList<>();

    /**
     * Get total number of pages
     */
    public int getTotalPages(Map<Integer, ?> vaultItems) {
        return Math.max(1, (VAULT_SIZE + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }

    /**
     * Get current page number (0-indexed)
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set current page
     */
    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, page);
    }

    /**
     * Go to next page
     */
    public boolean nextPage(int totalPages) {
        if (currentPage < totalPages - 1) {
            currentPage++;
            return true;
        }
        return false;
    }

    /**
     * Go to previous page
     */
    public boolean previousPage() {
        if (currentPage > 0) {
            currentPage--;
            return true;
        }
        return false;
    }

    /**
     * Get vault slot index from GUI slot index on current page
     */
    public int getVaultSlotFromGUI(int guiSlot) {
        if (guiSlot < 0 || guiSlot >= ITEMS_PER_PAGE) {
            return -1;
        }
        return currentPage * ITEMS_PER_PAGE + guiSlot;
    }

    /**
     * Get GUI slot index from vault slot index
     */
    public int getGUISlotFromVault(int vaultSlot) {
        if (vaultSlot < 0 || vaultSlot >= VAULT_SIZE) {
            return -1;
        }

        int pageOfSlot = vaultSlot / ITEMS_PER_PAGE;
        if (pageOfSlot != currentPage) {
            return -1; // Slot is on different page
        }

        return vaultSlot % ITEMS_PER_PAGE;
    }

    /**
     * Get items for current page from vault
     */
    public Map<Integer, ItemStack> getPageItems(Map<Integer, ItemStack> allItems) {
        Map<Integer, ItemStack> pageItems = new java.util.HashMap<>();
        
        int startSlot = currentPage * ITEMS_PER_PAGE;
        int endSlot = Math.min(startSlot + ITEMS_PER_PAGE, VAULT_SIZE);

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack item = allItems.get(i);
            if (item != null && !item.getType().isAir()) {
                pageItems.put(i - startSlot, item);
            }
        }

        return pageItems;
    }

    /**
     * Get items per page constant
     */
    public static int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }

    /**
     * Get inventory rows for items
     */
    public static int getInventoryRows() {
        return INVENTORY_ROWS;
    }
}
