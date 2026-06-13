package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BuildingConfigLoader {
    private static final Map<String, BuildingConfig> configs;

    static {
        configs = loadConfigs();
    }

    private static Map<String, BuildingConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = BuildingConfigLoader.class.getResourceAsStream("/buildings.json");
            BuildingConfig[] buildings = mapper.readValue(is, BuildingConfig[].class);

            Map<String, BuildingConfig> map = new HashMap<>();
            for (BuildingConfig building : buildings) {
                map.put(building.getName(), building);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static BuildingConfig getConfig(String name) {
        return configs.get(name);
    }

    public static Collection<BuildingConfig> getAll() {
        return configs.values();
    }
}
