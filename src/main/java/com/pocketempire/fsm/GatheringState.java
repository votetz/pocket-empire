package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.Comparator;
import java.util.List;

public class GatheringState implements State {

    private static final int GATHER_RADIUS = 2;
    private static final int GATHER_RANGE = 6;

    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() < unit.getMaxHp() * 0.5) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        int nearbyAllies = countNearbyAllies(unit, world);
        if (nearbyAllies >= getGatherThreshold(unit)) {
            Unit enemy = world.findNearestEnemy(unit);
            if (enemy != null) {
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
                if (dist <= unit.getRange()) {
                    unit.changeState(new AttackState(), UnitState.ATTACKING);
                } else if (unit.getRange() > 1) {
                    unit.changeState(new SkirmishState(), UnitState.SKIRMISH);
                } else {
                    unit.changeState(new ChaseState(), UnitState.CHASE);
                }
            } else {
                return;
            }
        }

        Unit target = findNearestAlly(unit, world);
        if (target == null) {
            unit.changeState(new WanderState(), UnitState.WANDER);
            return;
        }

        int distToAlly = HexUtils.getDistance(unit.getQ(), unit.getR(), target.getQ(), target.getR());
        if (distToAlly <= 1 || unit.getRemainingOD() <= 0) {
            return;
        }

        List<Pathfinder.Node> path = Pathfinder.findPath(
                world, unit.getQ(), unit.getR(), target.getQ(), target.getR(), unit);

        if (path != null && path.size() > 1) {
            for (int i = 1; i < path.size() && unit.getRemainingOD() > 0; i++) {
                Pathfinder.Node next = path.get(i);
                int cost = world.getMap().getTile(next.getQ(), next.getR()).getType().getMovementCost();
                if (unit.getRemainingOD() < cost) break;
                if (world.isTileOccupied(next.getQ(), next.getR(), unit)) break;

                int fromQ = unit.getQ();
                int fromR = unit.getR();
                unit.spendOD(cost);
                unit.setQ(next.getQ());
                unit.setR(next.getR());
                GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
            }
        }
    }

    private Unit findNearestAlly(Unit unit, World world) {
        return world.getAllUnits().stream()
                .filter(u -> u != unit && u.isAlive())
                .filter(u -> u.getFactionId().equals(unit.getFactionId()))
                .filter(u -> isCombatUnit(u.getUnitType()))
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
    }

    private int countNearbyAllies(Unit unit, World world) {
        int count = 0;
        for (Unit other : world.getAllUnits()) {
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
            case LIGHT, HEAVY, GUARDIAN, ARCHER, MAGE, SIEGE, DROMON, TRIREME -> true;
            default -> false;
        };
    }

    private int getGatherThreshold(Unit unit) {
        return switch (unit.getUnitType()) {
            case HEAVY, GUARDIAN -> 1;
            case LIGHT -> 1;
            case ARCHER, MAGE, SIEGE -> 2;
            default -> 0;
        };
    }

    @Override
    public void exit(Unit unit) {}
}
