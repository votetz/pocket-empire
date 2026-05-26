package com.wrathborn.fsm;

import com.wrathborn.entities.Unit;
import java.util.List;

public class IdleState implements State {

    @Override
    public void enter(Unit unit) {
        System.out.println(unit.getId() + " is now IDLE");
    }

    @Override
    public void update(Unit unit, List<Unit> allUnits) {
        Unit enemy = findNearestEnemy(unit, allUnits);
        if (enemy != null) {
            unit.setUnitState(UnitState.ATTACKING);
        }
    }

    @Override
    public void exit(Unit unit) {}

    private Unit findNearestEnemy(Unit unit, List<Unit> allUnits) {
        return allUnits.stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .findFirst()
                .orElse(null);
    }

}