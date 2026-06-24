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
import java.util.stream.Collectors;

public class IdleState implements State {

    private static final double FLEE_THRESHOLD = 0.5;
    private static final int ENTRENCH_RANGE = 5;
    private static final int WANDER_RANGE = 15;
    private static final int GATHER_RADIUS = 2;

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

        if(unit.getUnitType() == UnitType.SCOUT) {
            unit.changeState(new WanderState(), UnitState.WANDER);
            return;
        }

        if (unit.getHp() < unit.getMaxHp()) {
            healAtBorder(unit, world);
        }

        if (unit.getHp() <= unit.getMaxHp() * FLEE_THRESHOLD) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        if (unit.getUnitType() == UnitType.CATAPULT) {
            City enemyCity = world.findNearestEnemyCity(unit);
            if (enemyCity != null) {
                unit.changeState(new AttackState(), UnitState.ATTACKING);
                return;
            }
        }

        Unit foreign = world.findNearestForeign(unit);
        if (foreign == null) {
            unit.changeState(new WanderState(), UnitState.WANDER);
            return;
        }

        if (isCombatUnit(unit.getUnitType())) {
            int nearbyAllies = countNearbyAllies(unit, world);
            if (nearbyAllies < getGatherThreshold(unit)) {
                unit.changeState(new GatheringState(), UnitState.GATHERING);
                return;
            }
        }

        Unit enemy = world.findNearestHostile(unit);
        if (enemy == null) {
            int distToForeign = HexUtils.getDistance(unit.getQ(), unit.getR(), foreign.getQ(), foreign.getR());
            if (distToForeign > ENTRENCH_RANGE) {
                unit.changeState(new WanderState(), UnitState.WANDER);
                return;
            }
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
                        int healAmount = 1 + city.getHealBonus();
                        unit.restoreHp(healAmount);
                        GameEventBus.getInstance().publish(new GameEvent.UnitHealed(unit, healAmount));
                        return;
                    }
                }
            }
        }
    }

    private int countNearbyAllies(Unit unit, World world) {
        int count = 0;
        for (var other : world.getAllUnits()) {
            if (other == unit || !other.isAlive()) continue;
            if (!other.getFactionId().equals(unit.getFactionId())) continue;
            if (!isCombatUnit(other.getUnitType())) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), other.getQ(), other.getR());
            if (dist <= GATHER_RADIUS) count++;
        }
        return count;
    }

    private boolean isCombatUnit(UnitType type) {
        return switch (type) {
            case LIGHT, HEAVY, GUARDIAN, ARCHER, MAGE, CATAPULT, KNIGHT -> true;
            default -> false;
        };
    }

    private int getGatherThreshold(Unit unit) {
        return switch (unit.getUnitType()) {
            case HEAVY, GUARDIAN -> 1;
            case LIGHT -> 1;
            case ARCHER, MAGE, CATAPULT -> 2;
            default -> 0;
        };
    }

    @Override
    public void exit(Unit unit) {}
}