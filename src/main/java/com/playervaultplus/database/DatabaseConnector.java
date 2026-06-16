package com.playervaultplus.database;

import com.playervaultplus.PlayerVaultPlus;
import com.playervaultplus.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connector using HikariCP for connection pooling
 * Manages MySQL connections efficiently
 */
public class DatabaseConnector {

    private final PlayerVaultPlus plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;

    public DatabaseConnector(PlayerVaultPlus plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialize database connection pool
     */
    public void initialize() throws SQLException {
        try {
            String host = configManager.getMySQLHost();
            int port = configManager.getMySQLPort();
            String username = configManager.getMySQLUsername();
            String password = configManager.getMySQLPassword();
            String database = configManager.getMySQLDatabase();

            // Build JDBC URL
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true",
                host, port, database, configManager.getBoolean("database.mysql.use-ssl", false)
            );

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(configManager.getInt("database.mysql.max-pool-size", 10));
            config.setMinimumIdle(configManager.getInt("database.mysql.min-idle-connections", 5));
            config.setConnectionTimeout(configManager.getLong("database.mysql.connection-timeout", 30000));
            config.setIdleTimeout(configManager.getLong("database.mysql.idle-timeout", 600000));
            config.setMaxLifetime(configManager.getLong("database.mysql.max-lifetime", 1800000));
            config.setThreadFactory(r -> {
                Thread t = new Thread(r, "PlayerVaultPlus-DB-" + System.nanoTime());
                t.setDaemon(true);
                return t;
            });

            // Create data source
            this.dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Database connection pool initialized");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            throw new SQLException("Database initialization failed", e);
        }
    }

    /**
     * Get a connection from the pool
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed");
        }
    }

    /**
     * Check if connection is active
     */
    public boolean isConnected() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                return false;
            }
            try (Connection conn = getConnection()) {
                return conn.isValid(5);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            try (Connection conn = getConnection()) {
                return conn.isValid(5);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
