package com.playervaultplus.util;

import com.playervaultplus.filter.FilterType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Utility methods for item filtering and sorting
 */
public class ItemUtils {

    /**
     * Get sort priority for item type
     * Used in auto-sort feature
     */
    public static int getSortPriority(String itemType) {
        FilterType filterType = FilterType.getFilterType(Material.getMaterial(itemType));
        return switch (filterType) {
            case SWORD -> 1;
            case ARMOR -> 2;
            case TOOLS -> 3;
            case BOW -> 4;
            case FOOD -> 5;
            case ORES -> 6;
            case POTIONS -> 7;
            default -> 8;
        };
    }

    /**
     * Check if item is stackable
     */
    public static boolean isStackable(Material material) {
        return material.getMaxStackSize() > 1;
    }

    /**
     * Get max stack size for material
     */
    public static int getMaxStackSize(Material material) {
        return material.getMaxStackSize();
    }
}
