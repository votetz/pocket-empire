package com.wrathborn.units;

import com.wrathborn.entities.Unit;

public class MageUnit extends Unit {
    public MageUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                40,
                40,
                24,
                2,
                2,
                4,
                140,
                140,
                factionId
        );
    }
}
