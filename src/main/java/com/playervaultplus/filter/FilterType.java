package com.playervaultplus.filter;

import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * Enum representing all filter types available in the vault
 */
public enum FilterType {
    ALL("All Items"),
    SWORD("Swords"),
    ARMOR("Armor"),
    TOOLS("Tools"),
    BOW("Ranged Weapons"),
    FOOD("Food"),
    ORES("Ores & Metals"),
    POTIONS("Potions"),
    MISC("Miscellaneous");

    private final String displayName;

    FilterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determine the filter type for a given material
     */
    public static FilterType getFilterType(Material material) {
        if (material == null) {
            return MISC;
        }

        String name = material.name().toUpperCase();

        // Check for swords
        if (name.endsWith("_SWORD")) {
            return SWORD;
        }

        // Check for armor
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
            name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return ARMOR;
        }

        // Check for tools
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") ||
            name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            return TOOLS;
        }

        // Check for ranged weapons
        if (name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT")) {
            return BOW;
        }

        // Check for food
        if (isFoodItem(material)) {
            return FOOD;
        }

        // Check for ores and metals
        if (isOreOrMetal(material)) {
            return ORES;
        }

        // Check for potions
        if (isPotionItem(material)) {
            return POTIONS;
        }

        return MISC;
    }

    /**
     * Check if material is a food item
     */
    private static boolean isFoodItem(Material material) {
        return material.isEdible();
    }

    /**
     * Check if material is an ore, metal ingot, or gem
     */
    private static boolean isOreOrMetal(Material material) {
        String name = material.name().toUpperCase();
        
        return name.contains("_ORE") ||
               name.contains("INGOT") ||
               name.contains("NUGGET") ||
               name.contains("DIAMOND") ||
               name.contains("EMERALD") ||
               name.contains("QUARTZ") ||
               name.contains("RAW_") ||
               name.equals("COPPER_BLOCK") ||
               name.equals("IRON_BLOCK") ||
               name.equals("GOLD_BLOCK") ||
               name.equals("DIAMOND_BLOCK") ||
               name.equals("EMERALD_BLOCK");
    }

    /**
     * Check if material is a potion item
     */
    private static boolean isPotionItem(Material material) {
        String name = material.name().toUpperCase();
        return name.contains("POTION");
    }
}
