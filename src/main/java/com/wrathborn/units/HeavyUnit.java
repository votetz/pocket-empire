package com.wrathborn.units;

import com.wrathborn.entities.Unit;

public class HeavyUnit extends Unit {
    public HeavyUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                200,
                200,
                20,
                15,
                1,
                1,
                60,
                60,
                factionId
        );
    }
}
