package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CasusBelliConfigLoader {
    private static final Map<String, CasusBelliConfig> configs;

    static {
        configs = loadConfigs();
    }

    private static Map<String, CasusBelliConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = CasusBelliConfigLoader.class.getResourceAsStream("/casus_belli.json");
            CasusBelliConfig[] configs = mapper.readValue(is, CasusBelliConfig[].class);

            Map<String, CasusBelliConfig> map = new HashMap<>();
            for (CasusBelliConfig config : configs) {
                map.put(config.getId(), config);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static CasusBelliConfig getConfig(String id) {
        return configs.get(id);
    }

    public static Collection<CasusBelliConfig> getAll() {
        return configs.values();
    }
}
