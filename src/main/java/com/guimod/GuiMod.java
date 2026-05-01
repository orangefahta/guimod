package com.guimod;

import com.guimod.command.GuiCommand;
import com.guimod.menu.EditSession;
import com.guimod.menu.MenuOpener;
import com.guimod.menu.MenuStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("guimod");

    @Override
    public void onInitialize() {
        LOGGER.info("[GuiMod] Загружается...");

        MenuStorage.init();
        GuiCommand.register();

        // ПКМ с предметом у которого NBT тег GUI
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) return ActionResult.PASS;

            NbtComponent nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComp == null) return ActionResult.PASS;

            NbtCompound nbt = nbtComp.copyNbt();
            if (!nbt.contains("GUI")) return ActionResult.PASS;

            String menuId = nbt.getString("GUI");
            MenuOpener.open(sp, menuId, EditSession.Mode.OPEN);

            return ActionResult.SUCCESS;
        });

        LOGGER.info("[GuiMod] Готов! Команда: /guimod");
    }
}
