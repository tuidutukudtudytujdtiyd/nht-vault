package com.playervaultplus.filter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages item filtering for vault display
 * Only affects GUI display, not actual vault data
 */
public class ItemFilter {

    private FilterType currentFilter = FilterType.ALL;
    private final Map<Integer, ItemStack> filteredCache = new HashMap<>();
    private boolean cacheValid = false;

    /**
     * Set the current filter type
     */
    public void setFilterType(FilterType filterType) {
        if (this.currentFilter != filterType) {
            this.currentFilter = filterType;
            invalidateCache();
        }
    }

    /**
     * Get current filter type
     */
    public FilterType getCurrentFilter() {
        return currentFilter;
    }

    /**
     * Check if an item matches the current filter
     */
    public boolean matches(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        if (currentFilter == FilterType.ALL) {
            return true;
        }

        FilterType itemType = FilterType.getFilterType(item.getType());
        return itemType == currentFilter;
    }

    /**
     * Invalidate the filter cache
     */
    public void invalidateCache() {
        cacheValid = false;
        filteredCache.clear();
    }

    /**
     * Check if cache is valid
     */
    public boolean isCacheValid() {
        return cacheValid;
    }
}
