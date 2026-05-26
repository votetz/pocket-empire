package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.UnitState;

public class ArcherUnit extends Unit {
    public ArcherUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                10,
                10,
                3,
                1,
                2,
                2,
                3,
                factionId,
                UnitState.IDLE
                );
    }
}
