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
 * Handles the /pv command
 * Opens the player's vault GUI when executed
 */
public class VaultCommand implements CommandExecutor, TabCompleter {

    private final PlayerVaultPlus plugin;

    public VaultCommand(PlayerVaultPlus plugin) {
        this.plugin = plugin;
        plugin.getCommand("pv").setExecutor(this);
        plugin.getCommand("pv").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players!");
            return true;
        }

        try {
            // Get or create player's vault
            PlayerVault vault = plugin.getVaultManager().getOrCreateVault(player.getUniqueId());
            
            // Open vault GUI
            plugin.getGUIManager().openVaultGUI(player, vault, 0);
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to open vault!");
            plugin.getLogger().severe("Error opening vault for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
