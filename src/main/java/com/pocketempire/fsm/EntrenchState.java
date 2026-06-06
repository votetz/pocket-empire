package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.fsm.UnitState;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.Comparator;

public class EntrenchState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        Unit nearest = findNearestEnemy(unit, world);
        if (nearest == null) return;

        int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), nearest.getQ(), nearest.getR());

        if (dist <= unit.getRange()) {
            unit.changeState(new AttackState(), UnitState.ATTACKING);
        } else if (dist <= 5) {
            unit.changeState(new SkirmishState(), UnitState.SKIRMISH);
        }
    }

    @Override
    public void exit(Unit unit) {}

    private Unit findNearestEnemy(Unit unit, World world) {
        return world.getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(
                        unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
    }
}
