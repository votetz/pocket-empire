package com.pocketempire.fsm;

import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;
import java.util.List;

public class ChaseState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getHp() < unit.getMaxHp() * 0.5) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        com.pocketempire.entities.Unit target = null;
        int minDist = Integer.MAX_VALUE;

        for (var faction : world.getFactions()) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (Unit enemy : faction.getUnits()) {
                if (enemy.getHp() <= 0) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
                if (dist < minDist) {
                    minDist = dist;
                    target = enemy;
                }
            }
        }

        if (target != null) {
            if (minDist <= 1) {
                unit.changeState(new AttackState(), UnitState.ATTACKING);
            } else {
                List<Pathfinder.Node> path = Pathfinder.findPath(world, unit.getQ(), unit.getR(), target.getQ(), target.getR(), unit);
                if (path != null && path.size() > 1) {
                    Pathfinder.Node next = path.get(1);
                    unit.setQ(next.getQ());
                    unit.setR(next.getR());
                }
            }
        } else {
            unit.changeState(new IdleState(), UnitState.IDLE);
        }
    }

    @Override
    public void exit(Unit unit) {}
}

