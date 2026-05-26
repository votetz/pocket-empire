package com.wrathborn.units;

import com.wrathborn.entities.Unit;
import com.wrathborn.fsm.IdleState;
import com.wrathborn.fsm.UnitState;

public class ArcherUnit extends Unit {
    public ArcherUnit(String id, int q, int r, String factionId) {
        super(id, q, r,
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
