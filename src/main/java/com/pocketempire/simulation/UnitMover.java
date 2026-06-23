package com.pocketempire.simulation;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.tiles.TileType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.List;

public class UnitMover {

    public void moveUnitAlongPath(Unit unit, World world, int targetQ, int targetR, Faction faction) {
        if (unit.getBlinkRange() > 0) {
            blinkToward(unit, world, targetQ, targetR);
            return;
        }

        while (unit.getRemainingOD() > 0) {
            List<Pathfinder.Node> path = Pathfinder.findPath(
                    world,
                    unit.getQ(), unit.getR(),
                    targetQ, targetR,
                    unit, faction
            );

            if (path.size() <= 1) break;

            Pathfinder.Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();

            Tile tile = world.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();

            if (unit.getRemainingOD() < cost) break;

            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.spendOD(cost);
            unit.move(dq, dr);
            unit.setEmbarked(tile.getType().isWater());
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    private void blinkToward(Unit unit, World world, int targetQ, int targetR) {
        if (unit.getRemainingOD() <= 0) return;

        int bestQ = unit.getQ();
        int bestR = unit.getR();
        int bestDist = HexUtils.getDistance(unit.getQ(), unit.getR(), targetQ, targetR);

        int range = unit.getBlinkRange();
        for (int dq = -range; dq <= range; dq++) {
            for (int dr = Math.max(-range, -dq - range); dr <= Math.min(range, -dq + range); dr++) {
                if (dq == 0 && dr == 0) continue;
                int nq = unit.getQ() + dq;
                int nr = unit.getR() + dr;

                if (!world.getMap().isInBounds(nq, nr)) continue;
                Tile tile = world.getMap().getTile(nq, nr);
                if (tile == null) continue;
                if (tile.getType().isBlocksMovement()) continue;
                if (world.isTileOccupied(nq, nr, unit)) continue;

                int dist = HexUtils.getDistance(nq, nr, targetQ, targetR);
                if (dist < bestDist) {
                    bestDist = dist;
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

    public void moveUnitTowardEnemy(Unit unit, Faction faction, World aiWorld) {
        Unit target = aiWorld.findNearestEnemy(unit);
        if (target == null) return;
        moveUnitAlongPath(unit, aiWorld, target.getQ(), target.getR(), faction);
    }

    public void moveUnitTowardCity(Unit unit, Faction faction, World aiWorld) {
        City target = null;
        int minDist = Integer.MAX_VALUE;

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
            if (dist < minDist) {
                minDist = dist;
                target = city;
            }
        }

        if (target == null || minDist <= 1) return;
        moveUnitAlongPath(unit, aiWorld, target.getQ(), target.getR(), faction);
    }
}
