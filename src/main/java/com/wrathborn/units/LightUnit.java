package com.wrathborn.units;

import com.wrathborn.entities.Unit;

public class LightUnit extends Unit {
    public LightUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                80,
                80,
                10,
                5,
                4,
                1,
                120,
                120,
                factionId
        );
    }
}
