package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import java.util.Comparator;
import java.util.List;
import com.pocketempire.world.HexUtils;

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
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
    }
}