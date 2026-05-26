package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.UnitState;

public class HeavyUnit extends Unit {
    public HeavyUnit(String id, int x, int y, String factionId) {
        super(id, x, y,
                20,
                20,
                3,
                4,
                1,
                1,
                5,
                factionId,
                UnitState.IDLE
        );
    }
}
