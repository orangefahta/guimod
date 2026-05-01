package com.guimod.menu;

import com.guimod.GuiMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GuiScreenHandler extends GenericContainerScreenHandler {

    private final SimpleInventory menuInv;
    private final MenuData data;
    private final EditSession.Mode mode;
    private final String menuId;

    public GuiScreenHandler(int syncId, PlayerInventory playerInv, SimpleInventory inv,
                            ScreenHandlerType<?> type, MenuData data,
                            EditSession.Mode mode, String menuId) {
        super(type, syncId, playerInv, inv, inv.size() / 9);
        this.menuInv = inv;
        this.data = data;
        this.mode = mode;
        this.menuId = menuId;
        GuiMod.LOGGER.info("[GuiMod] GuiScreenHandler created | menu='{}' mode={} syncId={}", menuId, mode, syncId);
    }

    private void snapshotInventory() {
        for (int i = 0; i < menuInv.size(); i++) {
            ItemStack stack = menuInv.getStack(i);
            if (!stack.isEmpty()) {
                MenuData.SlotData sd = data.slots.computeIfAbsent(i, k -> new MenuData.SlotData());
                sd.item = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                sd.name = stack.getName().getString();
            } else {
                data.slots.remove(i);
            }
        }
        GuiMod.LOGGER.info("[GuiMod] snapshotInventory: saved {} slots", data.slots.size());
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity sp)) return;

        GuiMod.LOGGER.info("[GuiMod] onSlotClick | slot={} button={} action={} mode={}", slotIndex, button, actionType, mode);

        switch (mode) {
            case SHOW -> {
                GuiMod.LOGGER.info("[GuiMod] SHOW — click blocked");
            }

            case CREATE, EDIT -> {
                if (actionType == SlotActionType.PICKUP && button == 1
                        && slotIndex >= 0 && slotIndex < menuInv.size()) {
                    ItemStack clicked = menuInv.getStack(slotIndex);
                    if (!clicked.isEmpty()) {
                        String itemName = clicked.getName().getString();
                        GuiMod.LOGGER.info("[GuiMod] RMB in {}: slot={} item='{}'", mode, slotIndex, itemName);

                        snapshotInventory();

                        EditSession.pushModClose(sp.getUuid());
                        sp.closeHandledScreen();

                        EditSession.waitForChat(sp.getUuid(),
                            new EditSession.ChatInput(menuId, slotIndex, itemName));

                        GuiMod.LOGGER.info("[GuiMod] Waiting for command input for slot {}", slotIndex);
                        sp.sendMessage(Text.translatable(
                            "guimod.chat.enter_command", itemName
                        ), false);
                        return;
                    }
                }
                super.onSlotClick(slotIndex, button, actionType, player);
            }

            case OPEN -> {
                if (slotIndex < 0 || slotIndex >= menuInv.size()) {
                    GuiMod.LOGGER.info("[GuiMod] OPEN: click outside menu slot={}, ignoring", slotIndex);
                    return;
                }

                ItemStack clicked = menuInv.getStack(slotIndex);

                if (data.takeSlots.contains(slotIndex)) {
                    GuiMod.LOGGER.info("[GuiMod] OPEN: take-item slot={}", slotIndex);
                    if (!clicked.isEmpty()) {
                        boolean ok = sp.getInventory().insertStack(clicked.copy());
                        if (ok) menuInv.setStack(slotIndex, ItemStack.EMPTY);
                        GuiMod.LOGGER.info("[GuiMod] OPEN: item moved={}", ok);
                    }
                    return;
                }

                MenuData.SlotData slotData = data.slots.get(slotIndex);
                if (slotData == null || slotData.command == null) {
                    GuiMod.LOGGER.info("[GuiMod] OPEN: slot={} has no command, ignoring", slotIndex);
                    return;
                }

                GuiMod.LOGGER.info("[GuiMod] OPEN: executing command '{}' slot={}", slotData.command, slotIndex);
                EditSession.pushModClose(sp.getUuid());
                sp.getServer().getCommandManager().executeWithPrefix(
                    sp.getCommandSource(), slotData.command
                );
                sp.closeHandledScreen();
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        if (mode == EditSession.Mode.CREATE || mode == EditSession.Mode.EDIT) {
            return super.quickMove(player, slot);
        }
        if (mode == EditSession.Mode.OPEN && slot < menuInv.size()) {
            if (data.takeSlots.contains(slot)) return super.quickMove(player, slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!(player instanceof ServerPlayerEntity sp)) return;

        boolean wasModClose = EditSession.popModClose(sp.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onClosed | menu='{}' mode={} wasModClose={}", menuId, mode, wasModClose);

        if (wasModClose) {
            GuiMod.LOGGER.info("[GuiMod] onClosed: closed by mod, skipping save");
            return;
        }

        EditSession.ActiveSession session = EditSession.getSession(sp.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onClosed: session={}", session == null ? "null" : session.menuId + "/" + session.mode);
        if (session == null) return;

        if (mode == EditSession.Mode.CREATE || mode == EditSession.Mode.EDIT) {
            GuiMod.LOGGER.info("[GuiMod] onClosed: player closed via Esc, saving '{}'", session.menuId);
            snapshotInventory();
            MenuStorage.save(session.menuId, data);
            sp.sendMessage(Text.translatable("guimod.menu.saved", session.menuId), false);
            GuiMod.LOGGER.info("[GuiMod] onClosed: saved successfully!");
            EditSession.endSession(sp.getUuid());
        }
    }
}
