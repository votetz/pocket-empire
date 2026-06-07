package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.List;

public class WorkState implements State {
    private int targetQ = -1, targetR = -1;

    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        City nearestCity = findNearestFriendlyCity(unit, world);
        if (nearestCity == null) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        if (isOnWorkableTile(unit, world)) {
            Tile tile = world.getMap().getTile(unit.getQ(), unit.getR());
            tile.setImproved(true);
            GameEventBus.getInstance().publish(new GameEvent.TileImproved(tile, unit));
            findNewTarget(unit, nearestCity, world);
        }

        if (targetQ < 0 || !isWorkableTile(targetQ, targetR, unit, world)) {
            findNewTarget(unit, nearestCity, world);
            if (targetQ < 0) {
                unit.changeState(new IdleState(), UnitState.IDLE);
                return;
            }
        }

        if (unit.getRemainingOD() <= 0) return;

        List<Pathfinder.Node> path = Pathfinder.findPath(
                world, unit.getQ(), unit.getR(), targetQ, targetR, unit);
        if (path != null && path.size() > 1) {
            Pathfinder.Node next = path.get(1);
            Tile tile = world.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();
            if (unit.getRemainingOD() >= cost) {
                int fromQ = unit.getQ(), fromR = unit.getR();
                unit.spendOD(cost);
                unit.move(next.getQ() - fromQ, next.getR() - fromR);
                GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
            }
        }
    }

    private City findNearestFriendlyCity(Unit unit, World world) {
        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Faction f : world.getFactions()) {
            if (!String.valueOf(f.getId()).equals(unit.getFactionId())) continue;
            for (City c : f.getCities()) {
                if (!c.isAlive()) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), c.getQ(), c.getR());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = c;
                }
            }
        }
        return nearest;
    }

    private void findNewTarget(Unit unit, City nearestCity, World world) {
        targetQ = -1;
        targetR = -1;
        int bestDist = Integer.MAX_VALUE;

        for (int[] dir : HexUtils.DIRECTIONS) {
            int nq = nearestCity.getQ() + dir[0];
            int nr = nearestCity.getR() + dir[1];
            if (!isWorkableTile(nq, nr, unit, world)) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), nq, nr);
            if (dist < bestDist) {
                bestDist = dist;
                targetQ = nq;
                targetR = nr;
            }
        }
    }

    private boolean isWorkableTile(int q, int r, Unit unit, World world) {
        if (!world.getMap().isInBounds(q, r)) return false;
        Tile tile = world.getMap().getTile(q, r);
        if (tile == null || tile.getType().isBlocksMovement()) return false;
        if (tile.isImproved()) return false;
        return !world.isTileOccupied(q, r);
    }

    private boolean isOnWorkableTile(Unit unit, World world) {
        Tile tile = world.getMap().getTile(unit.getQ(), unit.getR());
        return tile != null && !tile.getType().isBlocksMovement() && !tile.isImproved();
    }

    @Override
    public void exit(Unit unit) {}
}
