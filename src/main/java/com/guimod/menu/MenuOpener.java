package com.guimod.menu;

import com.guimod.GuiMod;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MenuOpener {

    public static void open(ServerPlayerEntity player, String menuId, EditSession.Mode mode) {
        open(player, menuId, mode, null);
    }

    public static void open(ServerPlayerEntity player, String menuId, EditSession.Mode mode, MenuData existingData) {
        GuiMod.LOGGER.info("[GuiMod] MenuOpener.open | игрок='{}' меню='{}' режим={} existingData={}",
            player.getName().getString(), menuId, mode, existingData != null ? "передана" : "null");

        MenuData data;

        if (existingData != null) {
            data = existingData;
            GuiMod.LOGGER.info("[GuiMod] MenuOpener: используем переданные данные, слотов={}", data.slots.size());
        } else if (mode == EditSession.Mode.CREATE) {
            data = new MenuData();
            GuiMod.LOGGER.info("[GuiMod] MenuOpener: создаём новое пустое меню");
        } else {
            data = MenuStorage.load(menuId);
            if (data == null) {
                GuiMod.LOGGER.warn("[GuiMod] MenuOpener: меню '{}' не найдено!", menuId);
                player.sendMessage(Text.translatable("guimod.menu.not_found", menuId), false);
                return;
            }
            GuiMod.LOGGER.info("[GuiMod] MenuOpener: загружено с диска '{}', слотов={}", menuId, data.slots.size());
        }

        if (mode == EditSession.Mode.CREATE || mode == EditSession.Mode.EDIT) {
            EditSession.startSession(player.getUuid(), menuId, mode, data);
            GuiMod.LOGGER.info("[GuiMod] MenuOpener: сессия запущена для режима {}", mode);
        } else {
            EditSession.endSession(player.getUuid());
            GuiMod.LOGGER.info("[GuiMod] MenuOpener: сессия очищена для режима {}", mode);
        }

        SimpleInventory inv = buildInventory(data);
        MenuData finalData = data;

        // НЕТ pushModClose здесь — открытие меню не должно влиять на счётчик
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInv, p) -> new GuiScreenHandler(
                syncId, playerInv, inv, data.getScreenType(), finalData, mode, menuId
            ),
            Text.literal(getTitlePrefix(mode) + data.getTitle(menuId))
        ));

        GuiMod.LOGGER.info("[GuiMod] MenuOpener: экран открыт");
    }

    private static SimpleInventory buildInventory(MenuData data) {
        SimpleInventory inv = new SimpleInventory(data.getSize());

        for (var entry : data.slots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= data.getSize()) continue;

            MenuData.SlotData sd = entry.getValue();
            if (sd.item == null) continue;

            Item item = Registries.ITEM.get(Identifier.of(sd.item));
            if (item == Items.AIR) item = Items.STONE;

            ItemStack stack = new ItemStack(item);
            if (sd.name != null) {
                // ITEM_NAME не даёт эффекта "переименованного" предмета (курсив/фиолетовый),
                // в отличие от CUSTOM_NAME
                stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                    Text.literal(sd.name));
            }
            inv.setStack(slot, stack);
            GuiMod.LOGGER.info("[GuiMod] buildInventory: слот {} = '{}' ({})", slot, sd.name, sd.item);
        }

        return inv;
    }

    private static String getTitlePrefix(EditSession.Mode mode) {
        return switch (mode) {
            case CREATE -> "§a[CREATE] ";
            case EDIT   -> "§e[EDIT] ";
            case SHOW   -> "§7[SHOW] ";
            case OPEN   -> "";
        };
    }
}
