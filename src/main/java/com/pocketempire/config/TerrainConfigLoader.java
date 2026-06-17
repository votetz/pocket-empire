package com.pocketempire.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketempire.tiles.TileType;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public class TerrainConfigLoader {
    private static Map<TileType, int[]> bonuses;

    public static int getDefendBonus(TileType type) {
        if (bonuses == null) bonuses = load();
        return bonuses.getOrDefault(type, new int[]{0, 0})[0];
    }

    public static int getAttackModifier(TileType type) {
        if (bonuses == null) bonuses = load();
        return bonuses.getOrDefault(type, new int[]{0, 0})[1];
    }

    private static Map<TileType, int[]> load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = TerrainConfigLoader.class.getResourceAsStream("/terrain.json");
            Map<String, Map<String, Integer>> raw = mapper.readValue(is, new TypeReference<>() {});
            Map<TileType, int[]> result = new EnumMap<>(TileType.class);
            for (var entry : raw.entrySet()) {
                TileType type = TileType.valueOf(entry.getKey());
                int defend = entry.getValue().getOrDefault("defendBonus", 0);
                int attack = entry.getValue().getOrDefault("attackModifier", 0);
                result.put(type, new int[]{defend, attack});
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load terrain.json", e);
        }
    }
}
