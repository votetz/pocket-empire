package com.wrathborn.units;

import com.wrathborn.entities.Unit;

public class ArcherUnit extends Unit {
    public ArcherUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                60,
                60,
                18,
                3,
                2,
                4,
                100,
                100,
                factionId
                );
    }
}
