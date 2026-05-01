# GuiMod — Virtual Chest GUI System

A Fabric mod for Minecraft 1.21 - 1.21.8 that lets you create virtual chest menus triggered by items with custom NBT tags. No real chest block needed.

---

## Requirements
- Minecraft 1.21 - 1.21.8
- Fabric Loader 0.16+
- Fabric API

---

## Installation
1. Drop `guimod.jar` into your `.minecraft/mods/` folder
2. Launch the game
3. Menus are stored in `.minecraft/config/guimod/menus/`

---

## How to Open a Menu with an Item

Give any item a `GUI` NBT tag with the menu ID:
```
/give @s minecraft:compass[custom_data={GUI:"main_menu"}]
```
Right-click with that item → the menu opens.

---

## Commands

All commands require operator permission (level 2).

### `/guimod create <id>`
Creates a new empty menu.
- Opens a virtual chest — place items into any slots you want
- **Left-click** moves items freely
- **Right-click** on an item → menu closes, chat asks you to type a command for that slot
- Close the chest (Esc) → menu is saved automatically

### `/guimod edit <id>`
Same as `create` but opens an existing menu for editing.

### `/guimod delete <id>`
Deletes the menu file permanently.

### `/guimod show <id>`
Opens the menu as a preview — nothing is clickable.

### `/guimod open <id>`
Opens the menu as a player — clicks run commands, take-slots give items.

### `/guimod list`
Lists all saved menus.

---

## Config Commands

### `/guimod cfg <id> menu-type <type>`
Sets the chest GUI type.
- `chest` — 27 slots (default)
- `double` — 54 slots
- `enderchest` — 27 slots (ender chest appearance)

### `/guimod cfg <id> chest-name <name>`
Sets the title shown at the top of the chest GUI.
Supports color codes (`§a`, `§6`, etc.) and spaces.

Example:
```
/guimod cfg main_menu chest-name §6Main Menu
```

### `/guimod cfg <id> take-item <from> <to>`
Marks a range of slots where players can actually take the item into their inventory instead of triggering a command.

With the `delete` keyword, you can remove slots.

Example — allow taking items from slots 10 to 16:
```
/guimod cfg my_shop take-item 10 16
```
Example — deleting taking items from slots 10 to 16:
```
/guimod cfg my_shop take-item 10 16 delete
```
You can run this multiple times.

### `/guimod cfg <id> list`
Shows current config for a menu (type, slot count, take-slots).

---

## Setting Commands for Slots

When creating or editing a menu, **right-click** any item in the chest.
The menu closes and you'll see this in chat:

```
Enter the command for [Item Name] (without /).
To cancel type: cfg menu:/!/: exit
```

Type the command (without `/`) and press Enter.
The menu reopens automatically so you can continue editing.

Example — typing:
```
function pvpmap:give_kit_sword
```
...will run `/function pvpmap:give_kit_sword` when a player clicks that item.

---

## Example Workflow

1. Create a menu:
```
/guimod create kits_menu
```
2. Place a Diamond Sword in slot 13, close the chest
3. Right-click the sword → type `function pvpmap:give_kit_sword`
4. Set a title:
```
/guimod cfg kits_menu chest-name §6Choose a Kit
```
5. Give yourself a trigger item:
```
/give @s minecraft:compass[custom_data={GUI:"kits_menu"}]
```
6. Right-click the compass → menu opens, click the sword → kit given!

---

## Notes
- Commands run **as the player** who clicks — make sure they have permission or use `function` from a datapack
- Menus are saved as JSON files in `.minecraft/config/guimod/menus/` and can be edited manually
- Color codes use `§` (section sign), e.g. `§aGreen`, `§cRed`, `§6Gold`
- The mod supports English and Russian languages.
- Mod's versions is from 1.21 to 1.21.8.
