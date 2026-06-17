package com.pocketempire.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketempire.units.UnitRole;

import java.io.InputStream;
import java.util.*;

public class RoleConfigLoader {
    private static Map<UnitRole, Set<UnitRole>> counters;

    public static Map<UnitRole, Set<UnitRole>> getCounters() {
        if (counters == null) {
            counters = load();
        }
        return counters;
    }

    private static Map<UnitRole, Set<UnitRole>> load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = RoleConfigLoader.class.getResourceAsStream("/counters.json");
            Map<String, List<String>> raw = mapper.readValue(is, new TypeReference<>() {});
            Map<UnitRole, Set<UnitRole>> result = new EnumMap<>(UnitRole.class);
            for (var entry : raw.entrySet()) {
                UnitRole role = UnitRole.valueOf(entry.getKey());
                Set<UnitRole> targets = new HashSet<>();
                for (String target : entry.getValue()) {
                    targets.add(UnitRole.valueOf(target));
                }
                result.put(role, targets);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load counters.json", e);
        }
    }
}
