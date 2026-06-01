package com.pocketempire.units;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UnitConfigLoader {
    private static Map<String, UnitStats> configs = new HashMap<>();

    static {
        loadConfigs();
    }

    private static void loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = UnitConfigLoader.class.getResourceAsStream("/units.json");
            UnitStats[] units = mapper.readValue(is, UnitStats[].class);

            for (UnitStats unit : units) {
                configs.put(unit.getType(), unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UnitStats getConfig(String type) {
        return configs.get(type);
    }
}