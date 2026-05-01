package com.guimod.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MenuStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger("guimod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path menusDir;

    public static void init() {
        menusDir = FabricLoader.getInstance().getConfigDir().resolve("guimod").resolve("menus");
        try {
            Files.createDirectories(menusDir);
            LOGGER.info("[GuiMod] Папка меню: {}", menusDir);
        } catch (IOException e) {
            LOGGER.error("[GuiMod] Не удалось создать папку меню", e);
        }
    }

    public static void save(String id, MenuData data) {
        Path file = menusDir.resolve(id + ".json");
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(data, w);
        } catch (IOException e) {
            LOGGER.error("[GuiMod] Ошибка сохранения меню '{}': {}", id, e.getMessage());
        }
    }

    public static MenuData load(String id) {
        Path file = menusDir.resolve(id + ".json");
        if (!Files.exists(file)) return null;
        try (Reader r = Files.newBufferedReader(file)) {
            return GSON.fromJson(r, MenuData.class);
        } catch (IOException e) {
            LOGGER.error("[GuiMod] Ошибка загрузки меню '{}': {}", id, e.getMessage());
            return null;
        }
    }

    public static boolean delete(String id) {
        Path file = menusDir.resolve(id + ".json");
        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.error("[GuiMod] Ошибка удаления меню '{}': {}", id, e.getMessage());
            return false;
        }
    }

    public static boolean exists(String id) {
        return Files.exists(menusDir.resolve(id + ".json"));
    }

    public static List<String> listAll() {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(menusDir, "*.json")) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                result.add(name.substring(0, name.length() - 5));
            }
        } catch (IOException e) {
            LOGGER.error("[GuiMod] Ошибка листинга меню", e);
        }
        return result;
    }
}
