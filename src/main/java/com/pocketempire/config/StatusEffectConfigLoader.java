package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StatusEffectConfigLoader {
    private static final Map<String, StatusEffectConfig> configs;

    static {
        configs = loadConfigs();
    }

    private static Map<String, StatusEffectConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = StatusEffectConfigLoader.class.getResourceAsStream("/status_effects.json");
            StatusEffectConfig[] effects = mapper.readValue(is, StatusEffectConfig[].class);

            Map<String, StatusEffectConfig> map = new HashMap<>();
            for (StatusEffectConfig effect : effects) {
                map.put(effect.getName(), effect);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static StatusEffectConfig getConfig(String name) {
        return configs.get(name);
    }

    public static Collection<StatusEffectConfig> getAll() {
        return configs.values();
    }
}
