package net.minecraftforge.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Something a tool can do (dig with an axe, till with a hoe, ...). One instance per name. */
public final class ToolAction {
    private static final Map<String, ToolAction> ACTIONS = new ConcurrentHashMap<>();

    private final String name;

    private ToolAction(String name) {
        this.name = name;
    }

    public static ToolAction get(String name) {
        return ACTIONS.computeIfAbsent(name, ToolAction::new);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ToolAction[" + name + "]";
    }
}
