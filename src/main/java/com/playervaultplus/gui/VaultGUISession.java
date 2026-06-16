package com.playervaultplus.gui;

import com.playervaultplus.filter.FilterType;

import java.util.UUID;

/**
 * Stores GUI session data for a player
 * Persists across multiple GUI interactions
 */
public class VaultGUISession {

    private final UUID playerUUID;
    private int currentPage = 0;
    private FilterType currentFilter = FilterType.ALL;

    public VaultGUISession(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, page);
    }

    public FilterType getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(FilterType filter) {
        this.currentFilter = filter;
    }
}
