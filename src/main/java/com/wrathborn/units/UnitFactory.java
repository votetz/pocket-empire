package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.units.UnitType;

public class UnitFactory {
    public static Unit create(UnitType type, String id, int q, int r, String factionId) {
        UnitStats stats = UnitConfigLoader.getConfig(type.name());

        return new Unit.Builder(id, q, r, factionId)
                .config(stats)
                .build();
    }
}
