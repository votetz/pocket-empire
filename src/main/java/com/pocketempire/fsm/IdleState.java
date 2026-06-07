package com.pocketempire.fsm;

import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.Optional;

public class IdleState implements State {

    private static final double FLEE_THRESHOLD = 0.5;
    private static final int ENTRENCH_RANGE = 5;
    private static final int WANDER_RANGE = 15;

    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getUnitType() == UnitType.SETTLER) {
            unit.changeState(new SettleState(), UnitState.SETTLING);
            return;
        }
        if (unit.getUnitType() == UnitType.WORKER) {
            unit.changeState(new WorkState(), UnitState.WORKING);
            return;
        }

        if (unit.getHp() < unit.getMaxHp()) {
            healAtBorder(unit, world);
        }

        if (unit.getHp() <= unit.getMaxHp() * FLEE_THRESHOLD) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        Unit enemy = world.findNearestEnemy(unit);
        if (enemy == null) {
            unit.changeState(new WanderState(), UnitState.WANDER);
            return;
        }

        int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());

        if (dist > WANDER_RANGE) {
            unit.changeState(new WanderState(), UnitState.WANDER);
            return;
        }

        if (dist > ENTRENCH_RANGE) {
            unit.changeState(new EntrenchState(), UnitState.ENTRENCH);
            return;
        }

        if (unit.getRange() > 1) {
            unit.changeState(new SkirmishState(), UnitState.SKIRMISH);
        } else {
            unit.changeState(new AttackState(), UnitState.ATTACKING);
        }
    }

    private void healAtBorder(Unit unit, World world) {
        Optional<Faction> unitFaction = world.getFactions().stream()
                .filter(f -> String.valueOf(f.getId()).equals(unit.getFactionId()))
                .findFirst();

        if (unitFaction.isPresent()) {
            for (City city : unitFaction.get().getCities()) {
                if (city.isAlive()) {
                    int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                    if (dist <= city.getBorderRadius()) {
                        unit.restoreHp(1);
                        GameEventBus.getInstance().publish(new GameEvent.UnitHealed(unit, 1));
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void exit(Unit unit) {}
}