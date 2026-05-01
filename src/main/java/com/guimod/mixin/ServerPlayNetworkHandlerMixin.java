package com.guimod.mixin;

import com.guimod.GuiMod;
import com.guimod.menu.EditSession;
import com.guimod.menu.MenuData;
import com.guimod.menu.MenuOpener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        boolean waiting = EditSession.isWaitingChat(player.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onChatMessage | player='{}' waitingInput={} message='{}'",
            player.getName().getString(), waiting, packet.chatMessage());

        if (!waiting) return;

        String text = packet.chatMessage();
        EditSession.ChatInput input = EditSession.getChatInput(player.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onChatMessage: intercepting input for slot={} item='{}'",
            input.slot, input.itemName);

        EditSession.clearChatInput(player.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onChatMessage: chatInput cleared");

        // Cancel
        if (text.equalsIgnoreCase("cfg menu:/!/: exit")) {
            GuiMod.LOGGER.info("[GuiMod] onChatMessage: input cancelled");
            player.sendMessage(Text.translatable("guimod.chat.cancelled"), false);
            ci.cancel();
            EditSession.ActiveSession session = EditSession.getSession(player.getUuid());
            if (session != null) {
                GuiMod.LOGGER.info("[GuiMod] onChatMessage: reopening menu after cancel");
                MenuOpener.open(player, input.menuId, session.mode, session.data);
            }
            return;
        }

        ci.cancel();

        EditSession.ActiveSession session = EditSession.getSession(player.getUuid());
        GuiMod.LOGGER.info("[GuiMod] onChatMessage: session={}", session == null ? "null" : session.menuId + "/" + session.mode);

        if (session == null) {
            GuiMod.LOGGER.warn("[GuiMod] onChatMessage: session is null! Command lost.");
            return;
        }

        MenuData data = session.data;
        MenuData.SlotData slotData = data.slots.computeIfAbsent(input.slot, k -> new MenuData.SlotData());
        slotData.command = text;
        GuiMod.LOGGER.info("[GuiMod] onChatMessage: command '{}' saved to slot {}", text, input.slot);

        player.sendMessage(Text.translatable(
            "guimod.chat.command_saved", input.itemName, text
        ), false);

        GuiMod.LOGGER.info("[GuiMod] onChatMessage: reopening menu '{}'", input.menuId);
        MenuOpener.open(player, input.menuId, session.mode, session.data);
    }
}
