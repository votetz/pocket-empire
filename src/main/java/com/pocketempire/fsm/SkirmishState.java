package com.pocketempire.fsm;

import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.entities.Unit;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.simulation.CombatResolver;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.Comparator;
import java.util.List;

public class SkirmishState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() < unit.getMaxHp() * 0.5) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        // Find nearest living enemy
        Unit target = world.getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(
                        unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);

        if (target == null) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), target.getQ(), target.getR());
        int range = unit.getRange();

        if (dist <= range) {
            // Within attack range = shoot
            CombatResolver.resolveCombat(unit, target);

            // Enemy too close
            if (dist < range && unit.getRemainingOD() > 0) {
                retreatFrom(unit, target, world);
            }
        } else {
            // Enemy too far
            List<Pathfinder.Node> path = Pathfinder.findPath(
                    world, unit.getQ(), unit.getR(), target.getQ(), target.getR(), unit);
            if (path != null && path.size() > 1) {
                for (int i = 1; i < path.size() && unit.getRemainingOD() > 0; i++) {
                    Pathfinder.Node next = path.get(i);
                    int distAfterStep = HexUtils.getDistance(
                            next.getQ(), next.getR(), target.getQ(), target.getR());

                    if (distAfterStep < range) break;

                    Tile tile = world.getMap().getTile(next.getQ(), next.getR());
                    if (tile == null || tile.getType().isBlocksMovement()) break;

                    if (isTileOccupied(next.getQ(), next.getR(), unit, world)) break;

                    int cost = tile.getType().getMovementCost();
                    if (unit.getRemainingOD() < cost) break;

                    int fromQ = unit.getQ();
                    int fromR = unit.getR();
                    unit.spendOD(cost);
                    unit.setQ(next.getQ());
                    unit.setR(next.getR());
                    GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));

                    if (distAfterStep == range) break;
                }

                // Fire if now in range after advancing
                int newDist = HexUtils.getDistance(
                        unit.getQ(), unit.getR(), target.getQ(), target.getR());
                if (newDist <= range) {
                    CombatResolver.resolveCombat(unit, target);
                }
            }
        }

        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    private void retreatFrom(Unit unit, Unit target, World world) {
        if (unit.getRemainingOD() <= 0) return;

        int currentDist = HexUtils.getDistance(
                unit.getQ(), unit.getR(), target.getQ(), target.getR());

        int bestQ    = unit.getQ();
        int bestR    = unit.getR();
        int bestDist = currentDist;
        int bestCost = Integer.MAX_VALUE;

        for (int[] dir : HexUtils.DIRECTIONS) {
            int nq = unit.getQ() + dir[0];
            int nr = unit.getR() + dir[1];

            if (!world.getMap().isInBounds(nq, nr)) continue;

            Tile tile = world.getMap().getTile(nq, nr);
            if (tile == null || tile.getType().isBlocksMovement()) continue;

            // do not repeat
            if (isTileOccupied(nq, nr, unit, world)) continue;

            int cost    = tile.getType().getMovementCost();
            int newDist = HexUtils.getDistance(nq, nr, target.getQ(), target.getR());

            if (newDist > bestDist || (newDist == bestDist && cost < bestCost)) {
                bestDist = newDist;
                bestCost = cost;
                bestQ    = nq;
                bestR    = nr;
            }
        }

        if ((bestQ != unit.getQ() || bestR != unit.getR())
                && unit.getRemainingOD() >= bestCost) {
            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.spendOD(bestCost);
            unit.setQ(bestQ);
            unit.setR(bestR);
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    private boolean isTileOccupied(int q, int r, Unit self, World world) {
        for (Unit u : world.getAllUnits()) {
            if (u != self && u.isAlive() && u.getQ() == q && u.getR() == r) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exit(Unit unit) {}
}