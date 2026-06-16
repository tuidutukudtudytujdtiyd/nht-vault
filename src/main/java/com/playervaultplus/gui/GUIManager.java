package com.playervaultplus.gui;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.filter.FilterType;
import com.playervaultplus.vault.PlayerVault;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all GUI instances and player sessions
 */
public class GUIManager {

    private final PlayerVaultPlus plugin;
    private final Map<UUID, VaultGUISession> activeSessions = new HashMap<>();

    public GUIManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Open vault GUI for a player
     */
    public void openVaultGUI(Player player, PlayerVault vault, int page) {
        VaultGUISession session = activeSessions.computeIfAbsent(
            player.getUniqueId(),
            uuid -> new VaultGUISession(player.getUniqueId())
        );

        session.setCurrentPage(page);
        session.setCurrentFilter(FilterType.ALL);

        VaultGUI gui = new VaultGUI(plugin, player, vault, session);
        gui.open();
    }

    /**
     * Open filter GUI for a player
     */
    public void openFilterGUI(Player player, PlayerVault vault) {
        VaultGUISession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        FilterGUI gui = new FilterGUI(plugin, player, vault, session);
        gui.open();
    }

    /**
     * Close vault GUI session
     */
    public void closeVaultSession(UUID playerUUID) {
        VaultGUISession session = activeSessions.remove(playerUUID);
        if (session != null) {
            plugin.getVaultManager().saveVault(playerUUID);
        }
    }

    /**
     * Get active session for a player
     */
    public VaultGUISession getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    /**
     * Check if player has active vault GUI
     */
    public boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }
}
