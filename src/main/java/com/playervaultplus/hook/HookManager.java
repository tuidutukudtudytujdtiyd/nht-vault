package com.playervaultplus.hook;

import com.playervaultplus.PlayerVaultPlus;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Manages plugin hooks and integrations
 * Checks for third-party plugins and enables corresponding features
 */
public class HookManager {

    private final PlayerVaultPlus plugin;
    private MMOItemsHook mmoItemsHook;
    private boolean mmoItemsEnabled = false;

    public HookManager(PlayerVaultPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all available hooks
     */
    public void loadHooks() {
        plugin.getLogger().info("§6Loading plugin hooks...");
        
        // Check for MMOItems
        if (isPluginEnabled("MMOItems")) {
            try {
                mmoItemsHook = new MMOItemsHook(plugin);
                mmoItemsHook.initialize();
                mmoItemsEnabled = true;
                plugin.getLogger().info("§a✓ MMOItems hook loaded");
            } catch (Exception e) {
                plugin.getLogger().warning("§c✗ Failed to load MMOItems hook: " + e.getMessage());
                mmoItemsEnabled = false;
            }
        } else {
            plugin.getLogger().info("§7MMOItems not found, skipping hook");
        }
    }

    /**
     * Check if a plugin is enabled
     */
    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Get MMOItems hook
     */
    public MMOItemsHook getMMOItemsHook() {
        return mmoItemsHook;
    }

    /**
     * Check if MMOItems hook is enabled
     */
    public boolean isMMOItemsEnabled() {
        return mmoItemsEnabled;
    }
}
