package com.playervaultplus.vault;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Represents an item stored in the vault with serialization support
 */
public class VaultItem {

    private String serializedData;
    private int quantity;

    public VaultItem(ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            this.quantity = item.getAmount();
            this.serializedData = serializeItem(item);
        } else {
            this.quantity = 0;
            this.serializedData = null;
        }
    }

    public VaultItem(String serializedData, int quantity) {
        this.serializedData = serializedData;
        this.quantity = quantity;
    }

    /**
     * Serialize ItemStack to Base64 string for storage
     */
    public static String serializeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)
        ) {
            boos.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize item", e);
        }
    }

    /**
     * Deserialize Base64 string back to ItemStack
     */
    public static ItemStack deserializeItem(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)
        ) {
            return (ItemStack) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize item", e);
        }
    }

    public ItemStack toItemStack() {
        if (serializedData == null || serializedData.isEmpty()) {
            return null;
        }
        ItemStack item = deserializeItem(serializedData);
        if (item != null) {
            item.setAmount(quantity);
        }
        return item;
    }

    public String getSerializedData() {
        return serializedData;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isEmpty() {
        return serializedData == null || serializedData.isEmpty() || quantity <= 0;
    }
}
