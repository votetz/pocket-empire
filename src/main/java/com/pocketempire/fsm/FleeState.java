package com.pocketempire.fsm;

import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.List;

public class FleeState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() == unit.getMaxHp()) {
            unit.changeState(new IdleState(), UnitState.IDLE);
            return;
        }

        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : world.getFactions()) {
            if (!String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (City city : faction.getCities()) {
                if (!city.isAlive()) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = city;
                }
            }
        }

        if (nearest == null) return;

        if (minDist <= 1) {
            int healAmount = 2 + nearest.getHealBonus();
            unit.restoreHp(healAmount);
            GameEventBus.getInstance().publish(new GameEvent.UnitHealed(unit, healAmount));
        } else if (unit.getRemainingOD() > 0) {
            List<Pathfinder.Node> path = Pathfinder.findPath(
                    world, unit.getQ(), unit.getR(), nearest.getQ(), nearest.getR(), unit);
            if (path != null && path.size() > 1) {
                Pathfinder.Node next = path.get(1);
                int cost = world.getMap().getTile(next.getQ(), next.getR()).getType().getMovementCost();
                if (unit.getRemainingOD() >= cost && !world.isTileOccupied(next.getQ(), next.getR(), unit)) {
                    int fromQ = unit.getQ();
                    int fromR = unit.getR();
                    unit.spendOD(cost);
                    unit.setQ(next.getQ());
                    unit.setR(next.getR());
                    GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
                }
            }
        }
    }

    @Override
    public void exit(Unit unit) {}
}
