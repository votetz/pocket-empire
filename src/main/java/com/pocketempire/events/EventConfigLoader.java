package com.pocketempire.events;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class EventConfigLoader {
    private static ForestFireConfig forestFireConfig;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = EventConfigLoader.class.getResourceAsStream("/events.json");
            var tree = mapper.readTree(is);
            forestFireConfig = mapper.treeToValue(tree.get("forestFire"), ForestFireConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            forestFireConfig = ForestFireConfig.builder()
                    .chance(0.05).spreadChance(0.3)
                    .burnDuration(7).recoverDuration(20)
                    .damagePerTurn(3).build();
        }
    }

    public static ForestFireConfig getForestFireConfig() {
        return forestFireConfig;
    }
}
