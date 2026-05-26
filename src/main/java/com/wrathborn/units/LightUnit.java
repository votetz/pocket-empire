package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.UnitState;
import com.wrathborn.fsm.IdleState;

public class LightUnit extends Unit {
    public LightUnit(String id, int q, int r, String factionId) {
        super(id, q, r,
                12,
                12,
                3,
                2,
                2,
                1,
                3,
                factionId,
                UnitState.IDLE
        );
    }
}
