package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.IdleState;
import com.wrathborn.fsm.UnitState;

public class MageUnit extends Unit {
    public MageUnit(String id, int q, int r, String factionId) {
        super(id, q, r,
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
