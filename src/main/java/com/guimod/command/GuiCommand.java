package com.guimod.command;

import com.guimod.menu.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class GuiCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("guimod")
                    .requires(src -> src.hasPermissionLevel(2))

                    // /guimod create <id>
                    .then(CommandManager.literal("create")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

                                if (MenuStorage.exists(id)) {
                                    player.sendMessage(Text.translatable(
                                        "guimod.menu.already_exists", id
                                    ), false);
                                    return 0;
                                }

                                MenuOpener.open(player, id, EditSession.Mode.CREATE);
                                player.sendMessage(Text.translatable(
                                    "guimod.menu.creating", id
                                ), false);
                                return 1;
                            })
                        )
                    )

                    // /guimod delete <id>
                    .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                ServerCommandSource src = ctx.getSource();

                                if (MenuStorage.delete(id)) {
                                    src.sendFeedback(() -> Text.translatable("guimod.menu.deleted", id), false);
                                } else {
                                    src.sendFeedback(() -> Text.translatable("guimod.menu.not_found", id), false);
                                }
                                return 1;
                            })
                        )
                    )

                    // /guimod show <id>
                    .then(CommandManager.literal("show")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                MenuOpener.open(player, id, EditSession.Mode.SHOW);
                                return 1;
                            })
                        )
                    )

                    // /guimod edit <id>
                    .then(CommandManager.literal("edit")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                MenuOpener.open(player, id, EditSession.Mode.EDIT);
                                return 1;
                            })
                        )
                    )

                    // /guimod open <id>
                    .then(CommandManager.literal("open")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                MenuOpener.open(player, id, EditSession.Mode.OPEN);
                                return 1;
                            })
                        )
                    )

                    // /guimod cfg <id> ...
                    .then(CommandManager.literal("cfg")
                        .then(CommandManager.argument("id", StringArgumentType.word())

                            // /guimod cfg <id> menu-type <chest|double|enderchest>
                            .then(CommandManager.literal("menu-type")
                                .then(CommandManager.argument("type", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String id  = StringArgumentType.getString(ctx, "id");
                                        String type = StringArgumentType.getString(ctx, "type");
                                        ServerCommandSource src = ctx.getSource();

                                        if (!type.equals("chest") && !type.equals("double") && !type.equals("enderchest")) {
                                            src.sendFeedback(() -> Text.translatable("guimod.cfg.invalid_type"), false);
                                            return 0;
                                        }

                                        MenuData data = MenuStorage.load(id);
                                        if (data == null) {
                                            src.sendFeedback(() -> Text.translatable("guimod.menu.not_found_plain", id), false);
                                            return 0;
                                        }

                                        data.menuType = type;
                                        MenuStorage.save(id, data);
                                        src.sendFeedback(() -> Text.translatable("guimod.cfg.type_set", id, type), false);
                                        return 1;
                                    })
                                )
                            )

                            // /guimod cfg <id> chest-name <name>
                            .then(CommandManager.literal("chest-name")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        String id   = StringArgumentType.getString(ctx, "id");
                                        String name = StringArgumentType.getString(ctx, "name");
                                        ServerCommandSource src = ctx.getSource();
                                        MenuData data = MenuStorage.load(id);
                                        if (data == null) {
                                            src.sendFeedback(() -> Text.translatable("guimod.menu.not_found_plain", id), false);
                                            return 0;
                                        }
                                        data.chestName = name;
                                        MenuStorage.save(id, data);
                                        src.sendFeedback(() -> Text.translatable("guimod.cfg.name_set", id, name), false);
                                        return 1;
                                    })
                                )
                            )

                            // /guimod cfg <id> take-item <from> <to>
                            .then(CommandManager.literal("take-item")
                                .then(CommandManager.argument("from", StringArgumentType.word())
                                    .then(CommandManager.argument("to", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String id   = StringArgumentType.getString(ctx, "id");
                                            String from = StringArgumentType.getString(ctx, "from");
                                            String to   = StringArgumentType.getString(ctx, "to");
                                            ServerCommandSource src = ctx.getSource();

                                            MenuData data = MenuStorage.load(id);
                                            if (data == null) {
                                                src.sendFeedback(() -> Text.translatable("guimod.menu.not_found_plain", id), false);
                                                return 0;
                                            }

                                            try {
                                                int f = Integer.parseInt(from);
                                                int t = Integer.parseInt(to);
                                                for (int i = f; i <= t; i++) {
                                                    if (!data.takeSlots.contains(i)) {
                                                        data.takeSlots.add(i);
                                                    }
                                                }
                                                MenuStorage.save(id, data);
                                                src.sendFeedback(() -> Text.translatable(
                                                    "guimod.cfg.take_added", f, t, id
                                                ), false);
                                            } catch (NumberFormatException e) {
                                                src.sendFeedback(() -> Text.translatable("guimod.cfg.take_invalid"), false);
                                                return 0;
                                            }
                                            return 1;
                                        })

                                        .then(CommandManager.literal("delete")
                                            .executes(ctx -> {
                                                String id   = StringArgumentType.getString(ctx, "id");
                                                String from = StringArgumentType.getString(ctx, "from");
                                                String to   = StringArgumentType.getString(ctx, "to");
                                                ServerCommandSource src = ctx.getSource();

                                                MenuData data = MenuStorage.load(id);
                                                if (data == null) {
                                                    src.sendFeedback(() -> Text.translatable("guimod.menu.not_found_plain", id), false);
                                                    return 0;
                                                }

                                                try {
                                                    int f = Integer.parseInt(from);
                                                    int t = Integer.parseInt(to);
                                                    for (int i = f; i <= t; i++) {
                                                        data.takeSlots.remove((Integer) i);
                                                    }
                                                    MenuStorage.save(id, data);
                                                    src.sendFeedback(() -> Text.translatable(
                                                        "guimod.cfg.take_removed", f, t, id
                                                    ), false);
                                                } catch (NumberFormatException e) {
                                                    src.sendFeedback(() -> Text.translatable("guimod.cfg.take_delete_invalid"), false);
                                                    return 0;
                                                }
                                                return 1;
                                            })
                                        )
                                    )
                                )
                            )

                            // /guimod cfg <id> list
                            .then(CommandManager.literal("list")
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    ServerCommandSource src = ctx.getSource();

                                    MenuData data = MenuStorage.load(id);
                                    if (data == null) {
                                        src.sendFeedback(() -> Text.translatable("guimod.menu.not_found_plain", id), false);
                                        return 0;
                                    }

                                    src.sendFeedback(() -> Text.translatable(
                                        "guimod.cfg.list",
                                        id,
                                        data.menuType,
                                        String.valueOf(data.getSize()),
                                        String.valueOf(data.slots.size()),
                                        data.takeSlots.toString()
                                    ), false);
                                    return 1;
                                })
                            )
                        )
                    )

                    // /guimod list
                    .then(CommandManager.literal("list")
                        .executes(ctx -> {
                            List<String> all = MenuStorage.listAll();
                            ctx.getSource().sendFeedback(() -> Text.translatable(
                                "guimod.menu.list",
                                String.valueOf(all.size()), String.join(", ", all)
                            ), false);
                            return 1;
                        })
                    )
            );
        });
    }
}
