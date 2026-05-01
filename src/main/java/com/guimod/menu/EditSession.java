package com.guimod.menu;

import com.guimod.GuiMod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditSession {

    public enum Mode { CREATE, EDIT, SHOW, OPEN }

    public static class ChatInput {
        public String menuId;
        public int slot;
        public String itemName;
        public ChatInput(String menuId, int slot, String itemName) {
            this.menuId = menuId;
            this.slot = slot;
            this.itemName = itemName;
        }
    }

    public static class ActiveSession {
        public String menuId;
        public Mode mode;
        public MenuData data;
        public ActiveSession(String menuId, Mode mode, MenuData data) {
            this.menuId = menuId;
            this.mode = mode;
            this.data = data;
        }
    }

    private static final Map<UUID, ActiveSession> sessions = new HashMap<>();
    private static final Map<UUID, ChatInput> chatInputs = new HashMap<>();

    // Счётчик закрытий модом (вместо булевого флага)
    private static final Map<UUID, Integer> modCloseCount = new HashMap<>();

    public static void startSession(UUID player, String menuId, Mode mode, MenuData data) {
        modCloseCount.remove(player); // сбрасываем счётчик при новой сессии
        sessions.put(player, new ActiveSession(menuId, mode, data));
    }

    public static ActiveSession getSession(UUID player) { return sessions.get(player); }
    public static void endSession(UUID player) { sessions.remove(player); modCloseCount.remove(player); }
    public static boolean hasSession(UUID player) { return sessions.containsKey(player); }

    public static void waitForChat(UUID player, ChatInput input) { chatInputs.put(player, input); }
    public static ChatInput getChatInput(UUID player) { return chatInputs.get(player); }
    public static void clearChatInput(UUID player) { chatInputs.remove(player); }
    public static boolean isWaitingChat(UUID player) { return chatInputs.containsKey(player); }

    // Увеличиваем счётчик — мод собирается закрыть экран
    public static void pushModClose(UUID player) {
        int count = modCloseCount.getOrDefault(player, 0) + 1;
        modCloseCount.put(player, count);
        GuiMod.LOGGER.info("[GuiMod] EditSession.pushModClose | счётчик={}", count);
    }

    // onClosed проверяет — если счётчик > 0, это закрытие модом
    public static boolean popModClose(UUID player) {
        int count = modCloseCount.getOrDefault(player, 0);
        GuiMod.LOGGER.info("[GuiMod] EditSession.popModClose | счётчик до={}", count);
        if (count > 0) {
            modCloseCount.put(player, count - 1);
            return true;
        }
        return false;
    }
}
