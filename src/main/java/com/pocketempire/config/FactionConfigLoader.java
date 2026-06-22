package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FactionConfigLoader {
    private static final Map<String, FactionConfig> configs;

    static {
        configs = loadConfigs();
    }

    private static Map<String, FactionConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = FactionConfigLoader.class.getResourceAsStream("/factions.json");
            FactionConfig[] configs = mapper.readValue(is, FactionConfig[].class);

            Map<String, FactionConfig> map = new HashMap<>();
            for (FactionConfig config : configs) {
                map.put(config.getId(), config);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static FactionConfig getConfig(String id) {
        return configs.get(id);
    }

    public static Collection<FactionConfig> getAll() {
        return configs.values();
    }
}