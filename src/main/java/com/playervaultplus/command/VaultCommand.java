package com.playervaultplus.command;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.vault.PlayerVault;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /pv command and admin commands
 */
public class VaultCommand implements CommandExecutor, TabCompleter {

    private final PlayerVaultPlus plugin;

    public VaultCommand(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        plugin.getCommand("pv").setExecutor(this);
        plugin.getCommand("pv").setTabCompleter(this);
        plugin.getCommand("pvadmin").setExecutor(this);
        plugin.getCommand("pvadmin").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pvadmin")) {
            return handleAdminCommand(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players!");
            return true;
        }

        try {
            // Get or create player's vault
            PlayerVault vault = plugin.getVaultManager().getOrCreateVault(player.getUniqueId(), player.getName());
            
            // Open vault GUI
            plugin.getGUIManager().openVaultGUI(player, vault, 0);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ " + plugin.getConfigManager().getMessage("error").replace("%error%", e.getMessage()));
            plugin.getLogger().severe("Error opening vault for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handle admin commands
     */
    private boolean handleAdminCommand(org.bukkit.command.CommandSender sender, String[] args) {
        if (!sender.hasPermission("playervaultplus.admin")) {
            sender.sendMessage("§c✗ " + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /pvadmin <reload|info|migrate>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage("§a✓ Config reloaded!");
                break;
            case "info":
                try {
                    var stats = plugin.getDatabaseManager().getStats();
                    sender.sendMessage("§6=== PlayerVaultPlus Info ===");
                    sender.sendMessage("§eTotal Vaults: §a" + stats.get("totalVaults"));
                    sender.sendMessage("§eTotal Items: §a" + stats.get("totalItems"));
                    sender.sendMessage("§eCached Vaults: §a" + plugin.getVaultManager().getCachedVaultCount());
                } catch (Exception e) {
                    sender.sendMessage("§c✗ Error: " + e.getMessage());
                }
                break;
            default:
                sender.sendMessage("§eUnknown subcommand: " + subcommand);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("pvadmin") && args.length == 1) {
            completions.add("reload");
            completions.add("info");
            completions.add("migrate");
        }
        return completions;
    }
}
