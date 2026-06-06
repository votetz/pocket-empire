package com.pocketempire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UnitNamesLoader {
    private static final String NAMES_FILE = "/name_pools.json";
    private static NamesConfig config;
    private static final Set<String> usedNames = new HashSet<>();
    private static final Random random = new Random();

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
        if (config == null) return "Unknown Unit";

        if (!config.first_names.isEmpty()) {
            String name = config.first_names.remove(random.nextInt(config.first_names.size()));
            usedNames.add(name);
            return name;
        }

        if (config.prefixes != null && config.suffixes != null
                && !config.prefixes.isEmpty() && !config.suffixes.isEmpty()) {
            for (int attempt = 0; attempt < 100; attempt++) {
                String name = config.prefixes.get(random.nextInt(config.prefixes.size()))
                        + config.suffixes.get(random.nextInt(config.suffixes.size()));
                if (usedNames.add(name)) {
                    return name;
                }
            }
        }

        return "Unknown Unit";
    }
}