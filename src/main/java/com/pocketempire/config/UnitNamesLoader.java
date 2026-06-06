package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class UnitNamesLoader {
    private static final String NAMES_FILE = "/name_pools.json";
    private static NamesConfig config;
    static {
        loadNames();
    }

    private static void loadNames() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = UnitNamesLoader.class.getResourceAsStream(NAMES_FILE)) {
            config = mapper.readValue(is, NamesConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRandomName() {
        if (config == null || config.first_names.isEmpty()) return "Unknown Unit";

        String name = config.first_names.get(new java.util.Random().nextInt(config.first_names.size()));
        return name;
    }
}