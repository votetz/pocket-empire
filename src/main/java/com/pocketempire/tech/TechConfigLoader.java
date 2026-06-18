package com.pocketempire.tech;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TechConfigLoader {
    private static final Map<String, TechnologyConfig> configs;

    static {
        configs = loadConfigs();
    }

    private static Map<String, TechnologyConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = TechConfigLoader.class.getResourceAsStream("/technologies.json");
            TechnologyConfig[] techs = mapper.readValue(is, TechnologyConfig[].class);

            Map<String, TechnologyConfig> map = new HashMap<>();
            for (TechnologyConfig tech : techs) {
                map.put(tech.getId(), tech);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static TechnologyConfig getConfig(String id) {
        return configs.get(id);
    }

    public static Collection<TechnologyConfig> getAll() {
        return configs.values();
    }
}
