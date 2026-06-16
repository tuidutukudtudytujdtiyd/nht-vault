package com.playervaultplus.gui;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.filter.FilterType;
import com.playervaultplus.vault.PlayerVault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter selection GUI
 * Allows players to filter vault items by category
 */
public class FilterGUI {

    private static final int INVENTORY_SIZE = 27; // 3 rows
    private static final int BACK_BUTTON_SLOT = 26; // Last slot

    private final PlayerVaultPlus plugin;
    private final Player player;
    private final PlayerVault vault;
    private final VaultGUISession session;
    private Inventory inventory;

    public FilterGUI(PlayerVaultPlus plugin, Player player, PlayerVault vault, VaultGUISession session) {
        this.plugin = plugin;
        this.player = player;
        this.vault = vault;
        this.session = session;
    }

    /**
     * Open the filter GUI
     */
    public void open() {
        buildInventory();
        player.openInventory(inventory);
    }

    /**
     * Build the filter inventory
     */
    private void buildInventory() {
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, "§6Filter Items");

        // Add filter buttons
        int slot = 0;
        for (FilterType filter : FilterType.values()) {
            if (filter != FilterType.ALL) { // ALL filter handled separately
                ItemStack filterItem = createFilterButton(filter);
                inventory.setItem(slot, filterItem);
                slot++;
            }
        }

        // Add back button
        ItemStack backButton = createButton(Material.RED_CONCRETE, "§cBack to Vault");
        inventory.setItem(BACK_BUTTON_SLOT, backButton);
    }

    /**
     * Create a filter button
     */
    private ItemStack createFilterButton(FilterType filter) {
        Material material = getFilterMaterial(filter);
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        if (meta != null) {
            String displayName = session.getCurrentFilter() == filter ?
                "§a" + filter.getDisplayName() + " ✓" :
                "§e" + filter.getDisplayName();
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to filter");
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

    /**
     * Create a generic button
     */
    private ItemStack createButton(Material material, String name) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            button.setItemMeta(meta);
        }
        return button;
    }

    /**
     * Get appropriate material for filter type
     */
    private Material getFilterMaterial(FilterType filter) {
        return switch (filter) {
            case SWORD -> Material.DIAMOND_SWORD;
            case ARMOR -> Material.DIAMOND_CHESTPLATE;
            case TOOLS -> Material.DIAMOND_PICKAXE;
            case BOW -> Material.BOW;
            case FOOD -> Material.COOKED_BEEF;
            case ORES -> Material.DIAMOND;
            case POTIONS -> Material.POTION;
            case MISC -> Material.CHEST;
            default -> Material.BARRIER;
        };
    }

    /**
     * Handle inventory click
     */
    public void handleClick(int slot) {
        if (slot == BACK_BUTTON_SLOT) {
            // Return to vault GUI
            plugin.getGUIManager().openVaultGUI(player, vault, session.getCurrentPage());
            return;
        }

        // Filter buttons
        int filterIndex = slot;
        FilterType[] filters = FilterType.values();
        
        if (filterIndex < filters.length) {
            for (int i = 0, filterSlot = 0; i < filters.length; i++) {
                if (filters[i] != FilterType.ALL) {
                    if (filterSlot == filterIndex) {
                        session.setCurrentFilter(filters[i]);
                        // Refresh filter GUI
                        open();
                        return;
                    }
                    filterSlot++;
                }
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
