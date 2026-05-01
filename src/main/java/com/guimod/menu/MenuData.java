package com.guimod.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuData {

    // chest | double | enderchest
    public String menuType = "chest";

    // название сундука (показывается вверху GUI)
    public String chestName = null;

    // слоты: ключ = индекс слота, значение = конфиг слота
    public Map<Integer, SlotData> slots = new HashMap<>();

    // слоты из которых можно взять предмет
    public List<Integer> takeSlots = new ArrayList<>();

    public static class SlotData {
        public String item;
        public String name;
        public String command;
    }

    public int getSize() {
        return "double".equals(menuType) ? 54 : 27;
    }

    public net.minecraft.screen.ScreenHandlerType<?> getScreenType() {
        return switch (menuType) {
            case "double" -> net.minecraft.screen.ScreenHandlerType.GENERIC_9X6;
            default       -> net.minecraft.screen.ScreenHandlerType.GENERIC_9X3;
        };
    }

    public String getTitle(String id) {
        return chestName != null ? chestName : id;
    }
}
