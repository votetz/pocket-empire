package com.pocketempire.simulation;

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

public class UnitMover {

    public void moveUnitAlongPath(Unit unit, World world, int targetQ, int targetR) {
        while (unit.getRemainingOD() > 0) {
            List<Pathfinder.Node> path = Pathfinder.findPath(
                    world,
                    unit.getQ(), unit.getR(),
                    targetQ, targetR,
                    unit
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
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    public void moveUnitTowardEnemy(Unit unit, Faction faction, World aiWorld) {
        Unit target = aiWorld.findNearestEnemy(unit);
        if (target == null) return;
        moveUnitAlongPath(unit, aiWorld, target.getQ(), target.getR());
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
        moveUnitAlongPath(unit, aiWorld, target.getQ(), target.getR());
    }
}
