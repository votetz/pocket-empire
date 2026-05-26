package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.UnitState;

public class LightUnit extends Unit {
    public LightUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                15,
                15,
                3,
                2,
                2,
                1,
                2,
                factionId,
                UnitState.IDLE
        );
    }
}
