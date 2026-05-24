package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.units.UnitType;

public class UnitFactory {
    public static Unit create(UnitType type, String id, int x, int y, String factionId) {
        switch (type) {
            case LIGHT:
                return new LightUnit(id, x, y, factionId);
            case ARCHER:
                return new ArcherUnit(id, x, y, factionId);
            case HEAVY:
                return new HeavyUnit(id, x, y, factionId);
            default:
                return new LightUnit(id, x, y, factionId);
        }
    }
}
