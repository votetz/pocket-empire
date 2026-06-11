package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

public class WanderState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        Unit nearest = world.findNearestEnemy(unit);
        if (nearest != null) {
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), nearest.getQ(), nearest.getR());
            if (dist <= unit.getRange()) {
                unit.changeState(new AttackState(), UnitState.ATTACKING);
                return;
            }
            if (dist <= 5) {
                unit.changeState(new EntrenchState(), UnitState.ENTRENCH);
                return;
            }
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
