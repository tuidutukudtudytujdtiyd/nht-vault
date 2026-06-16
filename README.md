# PlayerVaultPlus

A comprehensive personal vault storage plugin for Paper 1.21.4+ with pagination and item filtering support.

## Features

✅ **300 Item Storage** - Each player has a personal vault with exactly 300 slots
✅ **Pagination System** - Navigate through items across multiple pages
✅ **Item Filtering** - Filter items by category (Sword, Armor, Tools, Bow, Food, Ores, Potions, Misc)
✅ **Persistent Storage** - Vault data is saved to disk and persists across server restarts
✅ **Anti-Dupe Protection** - Prevents item duplication through synchronized operations
✅ **GUI Interface** - User-friendly chest inventory GUI for vault management
✅ **Thread-Safe** - Safe concurrent access to vault data

## Commands

### `/pv` - Open Personal Vault
Aliases: `/pvault`, `/vault`

Opens your personal vault GUI.

## Installation

1. Build the plugin with Maven: `mvn clean package`
2. Copy the generated JAR file to your server's `plugins` folder
3. Restart the server
4. Vault data will be stored in `plugins/PlayerVaultPlus/vaults/`

## Usage

### Opening Vault
Simply execute `/pv` to open your personal vault.

### Managing Items
- **Left Click**: Pick up/place entire stack
- **Right Click**: Pick up/place single item
- **Page Navigation**: Use < Previous and Next > buttons to navigate pages
- **Filtering**: Click the Filter button (Hopper) to open filter menu

### Filtering Items
The filter menu allows you to view items by category:
- **All Items** - Show all items in vault
- **Swords** - All sword-type weapons
- **Armor** - Helmets, chestplates, leggings, boots
- **Tools** - Pickaxes, axes, shovels, hoes
- **Ranged Weapons** - Bows, crossbows, tridents
- **Food** - Edible items
- **Ores & Metals** - Raw ores, ingots, gems, metal blocks
- **Potions** - All potion types
- **Miscellaneous** - Other items

## Project Structure

```
src/main/java/com/playervaultplus/
├── PlayerVaultPlus.java          # Main plugin class
├── command/
│   └── VaultCommand.java         # /pv command handler
├── vault/
│   ├── PlayerVault.java          # Vault data model
│   ├── VaultItem.java            # Item wrapper with serialization
│   └── VaultManager.java         # Vault caching and management
├── gui/
│   ├── GUIManager.java           # GUI session management
│   ├── VaultGUI.java             # Main vault inventory GUI
│   ├── VaultGUISession.java      # Session state storage
│   └── FilterGUI.java            # Filter selection GUI
├── filter/
│   ├── FilterType.java           # Filter type enumeration
│   └── ItemFilter.java           # Item filtering logic
├── pagination/
│   └── PageManager.java          # Page calculation and management
├── data/
│   ├── VaultDataManager.java     # Data persistence manager
│   └── VaultDataHandler.java     # Serialization handler
└── listener/
    └── InventoryListener.java    # Inventory event handling
```

## Technical Details

### Architecture
- **OOP Design**: Clean separation of concerns with dedicated managers
- **Thread Safety**: Synchronized operations on vault data to prevent race conditions
- **Data Persistence**: Items are serialized to Base64 format and stored in text files
- **Anti-Dupe Protection**: Atomic operations prevent item duplication
- **Pagination**: 45 items per page (5 rows) with control row for navigation

### Storage Format
Vault data is stored in `plugins/PlayerVaultPlus/vaults/{UUID}.vault` files:
```
# PlayerVaultPlus Vault Data
# UUID: <player-uuid>
# Format: slot:quantity:serializedData

0:1:rO0ABXNyABdvcmcuYnVra2l0Lml0ZW0uSXRlbVN0YWNrAAAA...
```

### Configuration
No additional configuration needed. The plugin works out of the box.

## Compatibility

- **Server**: Paper 1.21.4+
- **Java**: Java 21+
- **API**: Paper API only (no external dependencies)

## Anti-Dupe Features

1. **Synchronized Operations**: All vault modifications are synchronized to prevent concurrent issues
2. **Item Clone**: Items are cloned when stored to prevent reference issues
3. **Atomic File Operations**: Temp files are used during save to prevent corruption
4. **Event Cancellation**: GUI events are properly cancelled to prevent unintended interactions
5. **Dirty Flag**: Only modified vaults are saved to disk

## Known Limitations

- GUI is limited to 45 items per page due to Minecraft's chest GUI constraints
- Filter view only affects display, not actual vault data
- Items must be right-clicked or left-clicked for proper handling

## Development

The plugin is production-ready with:
- Comprehensive error handling
- Detailed logging for debugging
- Clean code structure following Java conventions
- Extensive JavaDoc comments
- Thread-safe operations

## License

Free to use and modify for your server.
