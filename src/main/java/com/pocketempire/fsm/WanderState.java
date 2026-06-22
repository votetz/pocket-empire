package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

public class WanderState implements State {
    private int wanderTurns = 0;

    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        Unit nearestForeign = world.findNearestForeign(unit);
        if (nearestForeign != null) {
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), nearestForeign.getQ(), nearestForeign.getR());
            if (dist <= unit.getRange()) {
                Unit nearestHostile = world.findNearestHostile(unit);
                if (nearestHostile != null && HexUtils.getDistance(unit.getQ(), unit.getR(), nearestHostile.getQ(), nearestHostile.getR()) <= unit.getRange()) {
                    unit.changeState(new AttackState(), UnitState.ATTACKING);
                    return;
                }
            }
            if (dist <= 5) {
                Unit nearestHostile = world.findNearestHostile(unit);
                if (nearestHostile != null) {
                    unit.changeState(new EntrenchState(), UnitState.ENTRENCH);
                    return;
                }
            }
        }

        wanderTurns++;
        if (wanderTurns > 6 && (nearestForeign == null || HexUtils.getDistance(unit.getQ(), unit.getR(), nearestForeign.getQ(), nearestForeign.getR()) > 10)) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        if (unit.getRemainingOD() <= 0) return;

        java.util.Random random = new java.util.Random();
        int[][] directions = HexUtils.DIRECTIONS;
        int[] dir = directions[random.nextInt(directions.length)];

        int targetQ = unit.getQ() + dir[0];
        int targetR = unit.getR() + dir[1];

        if (world.getMap().isInBounds(targetQ, targetR)) {
            Tile tile = world.getMap().getTile(targetQ, targetR);
            int cost = tile.getType().getMovementCost();

            if (!tile.getType().isBlocksMovement() && unit.getRemainingOD() >= cost) {
                int fromQ = unit.getQ();
                int fromR = unit.getR();
                unit.spendOD(cost);
                unit.move(dir[0], dir[1]);
                GameEventBus.getInstance().publish(
                    new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR())
                );
            }
        }
    }

    @Override
    public void exit(Unit unit) {}
}
