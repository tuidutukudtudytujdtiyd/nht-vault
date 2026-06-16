package com.playervaultplus.listener;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.gui.FilterGUI;
import com.playervaultplus.gui.GUIManager;
import com.playervaultplus.gui.VaultGUI;
import com.playervaultplus.gui.VaultGUISession;
import com.playervaultplus.vault.PlayerVault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Handles inventory events for vault GUI interaction
 * Prevents item duplication and ensures data consistency
 */
public class InventoryListener implements Listener {

    private final PlayerVaultPlus plugin;
    private final GUIManager guiManager;

    public InventoryListener(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGUIManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handle inventory click events
     * Safely manages item movement to/from vault
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        VaultGUISession session = guiManager.getSession(playerUUID);

        // Check if player has active vault GUI
        if (session == null) {
            return;
        }

        PlayerVault vault = plugin.getVaultManager().getVault(playerUUID);
        if (vault == null) {
            return;
        }

        // Identify GUI type
        String invTitle = event.getView().getTitle();
        if (invTitle.contains("Vault")) {
            handleVaultGUIClick(event, player, vault, session);
        } else if (invTitle.contains("Filter")) {
            handleFilterGUIClick(event, player);
        }
    }

    /**
     * Handle vault GUI clicks
     */
    private void handleVaultGUIClick(InventoryClickEvent event, Player player, PlayerVault vault, VaultGUISession session) {
        int clickedSlot = event.getRawSlot();
        int inventorySize = event.getInventory().getSize();

        // Only handle clicks in the vault inventory
        if (clickedSlot >= inventorySize) {
            return;
        }

        // Handle control buttons (last row)
        if (clickedSlot >= 45) {
            event.setCancelled(true);
            handleVaultControlClick(clickedSlot, player, vault, session);
            return;
        }

        // Handle regular item slots
        handleVaultItemClick(event, player, vault, session, clickedSlot);
    }

    /**
     * Handle control button clicks in vault GUI
     */
    private void handleVaultControlClick(int slot, Player player, PlayerVault vault, VaultGUISession session) {
        switch (slot) {
            case 45: // Previous page
                if (session.getCurrentPage() > 0) {
                    session.setCurrentPage(session.getCurrentPage() - 1);
                    plugin.getGUIManager().openVaultGUI(player, vault, session.getCurrentPage());
                }
                break;
            case 53: // Next page
                int totalPages = (300 + 44) / 45; // Calculate total pages
                if (session.getCurrentPage() < totalPages - 1) {
                    session.setCurrentPage(session.getCurrentPage() + 1);
                    plugin.getGUIManager().openVaultGUI(player, vault, session.getCurrentPage());
                }
                break;
            case 49: // Filter button
                plugin.getGUIManager().openFilterGUI(player, vault);
                break;
        }
    }

    /**
     * Handle regular item clicks in vault GUI
     * Safely syncs items between GUI and vault
     */
    private void handleVaultItemClick(InventoryClickEvent event, Player player, PlayerVault vault, VaultGUISession session, int guiSlot) {
        // Calculate actual vault slot from GUI slot and current page
        int vaultSlot = session.getCurrentPage() * 45 + guiSlot;

        if (vaultSlot < 0 || vaultSlot >= 300) {
            event.setCancelled(true);
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        // Prevent duplication and ensure proper handling
        synchronized (vault) {
            if (event.isLeftClick()) {
                handleLeftClick(player, vault, vaultSlot, cursor, clicked, event);
            } else if (event.isRightClick()) {
                handleRightClick(player, vault, vaultSlot, cursor, clicked, event);
            }
        }
    }

    /**
     * Handle left click (pick up/place whole stack)
     */
    private void handleLeftClick(Player player, PlayerVault vault, int vaultSlot, ItemStack cursor, ItemStack clicked, InventoryClickEvent event) {
        if (cursor == null || cursor.getType().isAir()) {
            // Player is picking up an item
            ItemStack item = vault.removeItem(vaultSlot);
            if (item != null) {
                event.setCursor(item);
            }
        } else {
            // Player is placing an item
            if (vault.addItem(vaultSlot, cursor)) {
                event.setCursor(null);
            } else {
                event.setCancelled(true);
            }
        }
        event.setCancelled(true);
    }

    /**
     * Handle right click (pick up/place one item)
     */
    private void handleRightClick(Player player, PlayerVault vault, int vaultSlot, ItemStack cursor, ItemStack clicked, InventoryClickEvent event) {
        event.setCancelled(true);

        if (cursor == null || cursor.getType().isAir()) {
            if (clicked != null && !clicked.getType().isAir()) {
                // Take one item from vault
                ItemStack single = clicked.clone();
                single.setAmount(1);
                clicked.setAmount(clicked.getAmount() - 1);
                
                if (clicked.getAmount() <= 0) {
                    vault.removeItem(vaultSlot);
                } else {
                    vault.addItem(vaultSlot, clicked);
                }
                event.setCursor(single);
            }
        } else if (clicked == null || clicked.getType().isAir()) {
            // Place one item in vault
            ItemStack single = cursor.clone();
            single.setAmount(1);
            if (vault.addItem(vaultSlot, single)) {
                cursor.setAmount(cursor.getAmount() - 1);
                if (cursor.getAmount() <= 0) {
                    event.setCursor(null);
                }
            }
        }
    }

    /**
     * Handle filter GUI clicks
     */
    private void handleFilterGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        // FilterGUI handles its own logic through reopening
    }

    /**
     * Handle inventory close
     * Saves vault data when player closes GUI
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        VaultGUISession session = guiManager.getSession(playerUUID);

        if (session != null) {
            String invTitle = event.getView().getTitle();
            if (invTitle.contains("Vault")) {
                // Save vault when GUI closes
                plugin.getVaultManager().saveVault(playerUUID);
            }
        }
    }

    /**
     * Handle player disconnect
     * Ensures data is saved and session is cleaned up
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        
        // Save and unload vault
        if (guiManager.hasActiveSession(playerUUID)) {
            guiManager.closeVaultSession(playerUUID);
        } else {
            plugin.getVaultManager().unloadVault(playerUUID);
        }
    }
}
