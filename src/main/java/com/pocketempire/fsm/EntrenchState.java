package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.fsm.UnitState;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;



public class EntrenchState implements State {
    @Override
    public void enter(Unit unit) {
        unit.setDefenseModifier(unit.getDefense() / 2);
    }

    @Override
    public void update(Unit unit, World world) {
        Unit nearest = world.findNearestEnemy(unit);
        if (nearest == null) return;

        int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), nearest.getQ(), nearest.getR());

        if (dist <= unit.getRange()) {
            unit.changeState(new AttackState(), UnitState.ATTACKING);
        } else if (dist <= 5) {
            unit.changeState(new SkirmishState(), UnitState.SKIRMISH);
        }
    }

    @Override
    public void exit(Unit unit) {
        unit.setDefenseModifier(0);
    }
}
