package net.minecraftforge.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** What kind of soil a plant needs. */
public final class PlantType {
    private static final Pattern INVALID = Pattern.compile("[^a-zA-Z_]");
    private static final Map<String, PlantType> VALUES = new ConcurrentHashMap<>();

    public static final PlantType PLAINS = get("plains");
    public static final PlantType DESERT = get("desert");
    public static final PlantType BEACH = get("beach");
    public static final PlantType CAVE = get("cave");
    public static final PlantType WATER = get("water");
    public static final PlantType NETHER = get("nether");
    public static final PlantType CROP = get("crop");

    private final String name;

    private PlantType(String name) {
        this.name = name;
    }

    public static PlantType get(String name) {
        if (INVALID.matcher(name).find()) throw new IllegalArgumentException("PlantType.get() called with invalid name: " + name);
        return VALUES.computeIfAbsent(name, PlantType::new);
    }

    public String getName() {
        return name;
    }
}
