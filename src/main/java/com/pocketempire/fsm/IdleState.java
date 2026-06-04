package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.Comparator;

public class IdleState implements State {

    private static final double FLEE_THRESHOLD = 0.5;

    @Override
    public void enter(Unit unit) {
        System.out.println(unit.getId() + " is now IDLE");
    }

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() < unit.getMaxHp()) {
            healAtBorder(unit, world);
        }

        if (unit.getHp() <= unit.getMaxHp() * FLEE_THRESHOLD) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        Unit enemy = findNearestEnemy(unit, world.getAllUnits());
        if (enemy != null) {
            unit.changeState(new AttackState(), UnitState.ATTACKING);
        }
    }

    private void healAtBorder(Unit unit, World world) {
        outer:
        for (var faction : world.getFactions()) {
            if (!String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (City city : faction.getCities()) {
                if (!city.isAlive()) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                if (dist <= city.getBorderRadius()) {
                    unit.restoreHp(1);
                    System.out.println(unit.getId() + " healed (+1 HP, " + unit.getHp() + "/" + unit.getMaxHp() + ") at border of " + city.getName());
                    break outer;
                }
            }
        }
    }

    @Override
    public void exit(Unit unit) {

    }

    private Unit findNearestEnemy(Unit unit, java.util.List<Unit> allUnits) {
        return allUnits.stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
    }
}