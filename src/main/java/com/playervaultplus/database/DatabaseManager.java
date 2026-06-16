package com.playervaultplus.database;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.config.ConfigManager;
import com.playervaultplus.vault.PlayerVault;
import com.playervaultplus.vault.VaultItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages all database operations for vaults
 * Supports async operations for non-blocking I/O
 */
public class DatabaseManager {

    private final PlayerVaultPlus plugin;
    private final DatabaseConnector connector;
    private final ConfigManager configManager;

    public DatabaseManager(PlayerVaultPlus plugin, DatabaseConnector connector, ConfigManager configManager) {
        this.plugin = plugin;
        this.connector = connector;
        this.configManager = configManager;
    }

    /**
     * Initialize database and create tables if needed
     */
    public void initialize() throws SQLException {
        createTables();
    }

    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        String createVaultsTable = "CREATE TABLE IF NOT EXISTS vaults (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "player_uuid VARCHAR(36) UNIQUE NOT NULL," +
            "player_name VARCHAR(16) NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "used_slots INT DEFAULT 0," +
            "total_slots INT DEFAULT 300," +
            "INDEX idx_player_uuid (player_uuid)" +
            ")";

        String createItemsTable = "CREATE TABLE IF NOT EXISTS vault_items (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "vault_id INT NOT NULL," +
            "slot INT NOT NULL," +
            "item_type VARCHAR(100) NOT NULL," +
            "quantity INT NOT NULL," +
            "serialized_data LONGTEXT NOT NULL," +
            "display_name VARCHAR(255)," +
            "lore LONGTEXT," +
            "enchantments TEXT," +
            "custom_data TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "FOREIGN KEY (vault_id) REFERENCES vaults(id) ON DELETE CASCADE," +
            "UNIQUE KEY unique_slot (vault_id, slot)," +
            "INDEX idx_vault_id (vault_id)" +
            ")";

        String createLogsTable = "CREATE TABLE IF NOT EXISTS vault_logs (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "vault_id INT NOT NULL," +
            "action VARCHAR(50)," +
            "slot INT," +
            "item_type VARCHAR(100)," +
            "quantity INT," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (vault_id) REFERENCES vaults(id) ON DELETE CASCADE," +
            "INDEX idx_vault_id (vault_id)," +
            "INDEX idx_timestamp (timestamp)" +
            ")";

        try (Connection conn = connector.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createVaultsTable);
            stmt.execute(createItemsTable);
            stmt.execute(createLogsTable);
            plugin.getLogger().info("Database tables initialized");
        }
    }

    /**
     * Get or create vault ID for player
     */
    public int getOrCreateVaultId(UUID playerUUID, String playerName) throws SQLException {
        String selectQuery = "SELECT id FROM vaults WHERE player_uuid = ?";
        
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Create new vault
        String insertQuery = "INSERT INTO vaults (player_uuid, player_name, total_slots) VALUES (?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, playerName);
            stmt.setInt(3, 300);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create vault");
    }

    /**
     * Load vault from database
     */
    public PlayerVault loadVault(UUID playerUUID, String playerName) throws SQLException {
        PlayerVault vault = new PlayerVault(playerUUID);
        int vaultId = getOrCreateVaultId(playerUUID, playerName);

        String query = "SELECT slot, item_type, quantity, serialized_data, display_name, lore, enchantments, custom_data " +
            "FROM vault_items WHERE vault_id = ? ORDER BY slot";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vaultId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int slot = rs.getInt("slot");
                    String serializedData = rs.getString("serialized_data");
                    int quantity = rs.getInt("quantity");
                    String displayName = rs.getString("display_name");
                    String lore = rs.getString("lore");

                    VaultItem item = new VaultItem(serializedData, quantity);
                    item.setDisplayName(displayName);
                    item.setLore(lore);
                    
                    org.bukkit.inventory.ItemStack itemStack = item.toItemStack();
                    if (itemStack != null) {
                        vault.addItem(slot, itemStack, displayName, lore);
                    }
                }
            }
        }

        vault.setVaultId(vaultId);
        vault.setClean();
        return vault;
    }

    /**
     * Save vault to database asynchronously
     */
    public CompletableFuture<Boolean> saveVaultAsync(PlayerVault vault) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                saveVault(vault);
                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save vault: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Save vault to database
     */
    public void saveVault(PlayerVault vault) throws SQLException {
        int vaultId = vault.getVaultId();
        if (vaultId <= 0) return;

        // Delete existing items
        String deleteQuery = "DELETE FROM vault_items WHERE vault_id = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, vaultId);
            stmt.executeUpdate();
        }

        // Insert new items
        String insertQuery = "INSERT INTO vault_items (vault_id, slot, item_type, quantity, serialized_data, display_name, lore) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            Map<Integer, VaultItem> items = vault.getAllItems();
            for (Map.Entry<Integer, VaultItem> entry : items.entrySet()) {
                VaultItem vaultItem = entry.getValue();
                if (vaultItem != null && !vaultItem.isEmpty()) {
                    stmt.setInt(1, vaultId);
                    stmt.setInt(2, entry.getKey());
                    stmt.setString(3, vaultItem.getItemType());
                    stmt.setInt(4, vaultItem.getQuantity());
                    stmt.setString(5, vaultItem.getSerializedData());
                    stmt.setString(6, vaultItem.getDisplayName());
                    stmt.setString(7, vaultItem.getLore());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }

        // Update used slots
        String updateQuery = "UPDATE vaults SET used_slots = ? WHERE id = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, vault.getUsedSlots());
            stmt.setInt(2, vaultId);
            stmt.executeUpdate();
        }

        vault.setClean();
    }

    /**
     * Log vault action to database
     */
    public void logAction(int vaultId, String action, int slot, String itemType, int quantity) throws SQLException {
        String query = "INSERT INTO vault_logs (vault_id, action, slot, item_type, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vaultId);
            stmt.setString(2, action);
            stmt.setInt(3, slot);
            stmt.setString(4, itemType);
            stmt.setInt(5, quantity);
            stmt.executeUpdate();
        }
    }

    /**
     * Get database statistics
     */
    public Map<String, Object> getStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = connector.getConnection()) {
            // Total vaults
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM vaults")) {
                if (rs.next()) {
                    stats.put("totalVaults", rs.getInt("count"));
                }
            }

            // Total items
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM vault_items")) {
                if (rs.next()) {
                    stats.put("totalItems", rs.getInt("count"));
                }
            }
        }
        return stats;
    }
}
