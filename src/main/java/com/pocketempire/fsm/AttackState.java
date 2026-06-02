package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;
import com.pocketempire.simulation.CombatResolver;

import java.util.Comparator;

public class AttackState implements State {
    @Override
    public void enter(Unit unit) {
        System.out.println(unit.getId() + " is now ATTACKING");
    }

    @Override
    public void update(Unit unit, World world) {
        Unit enemy = world.getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(
                        unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);

        if (enemy != null) {
            int dist = HexUtils.getDistance(
                    unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
            if (dist <= unit.getRange()) {
                CombatResolver.resolveCombat(unit, enemy);
            }
        }

        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    @Override
    public void exit(Unit unit) {

    }
}
