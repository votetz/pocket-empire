package com.pocketempire.fsm;

import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.entities.Unit;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.simulation.CombatResolver;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

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

        Unit target = world.findNearestEnemy(unit);

        if (target == null) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), target.getQ(), target.getR());
        int range = unit.getRange();

        if (dist <= range) {
            CombatResolver.resolveCombat(unit, target,
                    world.getMap().getTile(target.getQ(), target.getR()).getType().getDefendBonus());

            if (unit.getBlinkRange() > 0 && unit.getRemainingOD() > 0) {
                blinkAwayFrom(unit, target, world);
            } else if (dist < range && unit.getRemainingOD() > 0) {
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

                    if (world.isTileOccupied(next.getQ(), next.getR(), unit)) break;

                    int cost = tile.getType().getMovementCost();
                    if (unit.getRemainingOD() < cost) break;

                    int fromQ = unit.getQ();
                    int fromR = unit.getR();
                    unit.spendOD(cost);
                    unit.setQ(next.getQ());
                    unit.setR(next.getR());
                    GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR,
                            unit.getQ(), unit.getR()));

                    if (distAfterStep == range) break;
                }

                // Fire if now in range after advancing
                int newDist = HexUtils.getDistance(
                        unit.getQ(), unit.getR(), target.getQ(), target.getR());
                if (newDist <= range) {
                    CombatResolver.resolveCombat(unit, target,
                            world.getMap().getTile(target.getQ(), target.getR()).getType().getDefendBonus());
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

            if (world.isTileOccupied(nq, nr, unit)) continue;

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

    private void blinkAwayFrom(Unit unit, Unit enemy, World world) {
        int bestQ = unit.getQ();
        int bestR = unit.getR();
        int bestScore = -9999;

        int range = unit.getBlinkRange();
        for (int dq = -range; dq <= range; dq++) {
            for (int dr = Math.max(-range, -dq - range); dr <= Math.min(range, -dq + range); dr++) {
                if (dq == 0 && dr == 0) continue;
                int nq = unit.getQ() + dq;
                int nr = unit.getR() + dr;

                if (!world.getMap().isInBounds(nq, nr)) continue;
                Tile tile = world.getMap().getTile(nq, nr);
                if (tile == null || tile.getType().isBlocksMovement()) continue;
                if (world.isTileOccupied(nq, nr, unit)) continue;

                int distFromEnemy = HexUtils.getDistance(nq, nr, enemy.getQ(), enemy.getR());
                int distToSelf = HexUtils.getDistance(nq, nr, unit.getQ(), unit.getR());
                int score = distFromEnemy * 2 - distToSelf;

                if (score > bestScore) {
                    bestScore = score;
                    bestQ = nq;
                    bestR = nr;
                }
            }
        }

        if (bestQ != unit.getQ() || bestR != unit.getR()) {
            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.setQ(bestQ);
            unit.setR(bestR);
            unit.spendOD(unit.getRemainingOD());
            GameEventBus.getInstance().publish(new GameEvent.MageBlinked(unit, fromQ, fromR, bestQ, bestR));
        }
    }

    @Override
    public void exit(Unit unit) {}
}