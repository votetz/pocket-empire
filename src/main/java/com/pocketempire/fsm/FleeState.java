package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

public class FleeState implements State {

    @Override
    public void enter(Unit unit) {
        System.out.println(unit.getName() + " is now FLEEING");
    }

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() == unit.getMaxHp()) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : world.getFactions()) {
            if (!String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (City city : faction.getCities()) {
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = city;
                }
            }
        }

        if (nearest != null && minDist <= 1) {
            unit.restoreHp(2);
            System.out.println(unit.getName() + " healed at " + nearest.getName()
                    + " (" + unit.getHp() + "/" + unit.getMaxHp() + " HP)");
        }
    }

    @Override
    public void exit(Unit unit) {

    }
}
