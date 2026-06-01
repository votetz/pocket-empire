package com.pocketempire.units;

import com.pocketempire.entities.Unit;
import com.pocketempire.units.UnitType;

public class UnitFactory {
    public static Unit create(UnitType type, String id, int q, int r, String factionId) {
        UnitStats stats = UnitConfigLoader.getConfig(type.name());

        return new Unit.Builder(id, q, r, factionId)
                .config(stats)
                .build();
    }
}
