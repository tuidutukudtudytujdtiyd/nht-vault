package com.playervaultplus.gui;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.filter.FilterType;
import com.playervaultplus.filter.ItemFilter;
import com.playervaultplus.pagination.PageManager;
import com.playervaultplus.vault.PlayerVault;
import com.playervaultplus.vault.VaultItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main vault GUI with pagination and filtering support
 * Displays 45 items per page with control buttons
 */
public class VaultGUI {

    private static final int INVENTORY_SIZE = 54; // 6 rows
    private static final int CONTROL_ROW_START = 45; // Row 6 for controls

    // Control button slots
    private static final int PREV_PAGE_SLOT = 45; // First slot of control row
    private static final int NEXT_PAGE_SLOT = 53; // Last slot of control row
    private static final int FILTER_BUTTON_SLOT = 49; // Middle of control row
    private static final int PAGE_INFO_SLOT = 48; // Info slot

    private final PlayerVaultPlus plugin;
    private final Player player;
    private final PlayerVault vault;
    private final VaultGUISession session;
    private final PageManager pageManager;
    private final ItemFilter itemFilter;
    private Inventory inventory;

    public VaultGUI(PlayerVaultPlus plugin, Player player, PlayerVault vault, VaultGUISession session) {
        this.plugin = plugin;
        this.player = player;
        this.vault = vault;
        this.session = session;
        this.pageManager = new PageManager();
        this.itemFilter = new ItemFilter();
        this.itemFilter.setFilterType(session.getCurrentFilter());
    }

    /**
     * Open the vault GUI for the player
     */
    public void open() {
        buildInventory();
        player.openInventory(inventory);
    }

    /**
     * Build the inventory with items and controls
     */
    private void buildInventory() {
        int totalPages = pageManager.getTotalPages(vault.getAllItems());
        int currentPage = session.getCurrentPage();

        // Validate page number
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
            session.setCurrentPage(currentPage);
        }
        pageManager.setCurrentPage(currentPage);

        // Create inventory
        String pageTitle = String.format("§6Vault §f(%d/%d)", currentPage + 1, totalPages);
        inventory = Bukkit.createInventory(player, INVENTORY_SIZE, pageTitle);

        // Add vault items
        addVaultItems();

        // Add control buttons
        addControlButtons(totalPages);
    }

    /**
     * Add vault items to the GUI
     */
    private void addVaultItems() {
        Map<Integer, VaultItem> vaultItems = vault.getAllItems();
        int startSlot = pageManager.getCurrentPage() * PageManager.getItemsPerPage();
        int endSlot = Math.min(startSlot + PageManager.getItemsPerPage(), 300);

        for (int vaultSlot = startSlot; vaultSlot < endSlot; vaultSlot++) {
            VaultItem vaultItem = vaultItems.get(vaultSlot);
            if (vaultItem != null && !vaultItem.isEmpty()) {
                ItemStack item = vaultItem.toItemStack();
                if (itemFilter.matches(item)) {
                    int guiSlot = vaultSlot - startSlot;
                    inventory.setItem(guiSlot, item);
                }
            }
        }
    }

    /**
     * Add control buttons to the GUI
     */
    private void addControlButtons(int totalPages) {
        int currentPage = pageManager.getCurrentPage();

        // Previous page button
        if (currentPage > 0) {
            ItemStack prevButton = createButton(Material.ARROW, "§c< Previous");
            inventory.setItem(PREV_PAGE_SLOT, prevButton);
        }

        // Next page button
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = createButton(Material.ARROW, "§aNext >");
            inventory.setItem(NEXT_PAGE_SLOT, nextButton);
        }

        // Filter button
        ItemStack filterButton = createButton(
            Material.HOPPER,
            "§bFilter: §e" + session.getCurrentFilter().getDisplayName()
        );
        inventory.setItem(FILTER_BUTTON_SLOT, filterButton);

        // Page info
        String pageInfoText = String.format(
            "§6Page %d/%d §8(Slots: %d/%d)",
            currentPage + 1,
            totalPages,
            vault.getUsedSlots(),
            300
        );
        ItemStack pageInfo = createButton(Material.PAPER, pageInfoText);
        inventory.setItem(PAGE_INFO_SLOT, pageInfo);
    }

    /**
     * Create a control button
     */
    private ItemStack createButton(Material material, String name) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to interact");
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

    /**
     * Handle inventory click
     */
    public void handleClick(int slot, ItemStack clicked) {
        if (slot >= CONTROL_ROW_START) {
            handleControlClick(slot);
            return;
        }

        // Regular item slot - no action needed, player can take/put items
    }

    /**
     * Handle control button click
     */
    private void handleControlClick(int slot) {
        int totalPages = pageManager.getTotalPages(vault.getAllItems());

        if (slot == PREV_PAGE_SLOT) {
            if (pageManager.previousPage()) {
                session.setCurrentPage(pageManager.getCurrentPage());
                open();
            }
        } else if (slot == NEXT_PAGE_SLOT) {
            if (pageManager.nextPage(totalPages)) {
                session.setCurrentPage(pageManager.getCurrentPage());
                open();
            }
        } else if (slot == FILTER_BUTTON_SLOT) {
            // Open filter GUI
            plugin.getGUIManager().openFilterGUI(player, vault);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getControlRowStart() {
        return CONTROL_ROW_START;
    }

    public ItemFilter getItemFilter() {
        return itemFilter;
    }

    public PageManager getPageManager() {
        return pageManager;
    }
}
