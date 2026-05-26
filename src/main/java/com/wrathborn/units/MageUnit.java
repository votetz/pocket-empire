package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.UnitState;

public class MageUnit extends Unit {
    public MageUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                10,
                10,
                5,
                1,
                1,
                2,
                5,
                factionId,
                UnitState.IDLE
        );
    }
}
