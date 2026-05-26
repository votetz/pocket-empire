package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.IdleState;
import com.wrathborn.fsm.UnitState;

public class HeavyUnit extends Unit {
    public HeavyUnit(String id, int q, int r, String factionId) {
        super(id, q, r,
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
