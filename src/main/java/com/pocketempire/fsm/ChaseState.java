package com.pocketempire.fsm;

import com.pocketempire.pathfinding.Pathfinder;
import java.util.List;

public class ChaseState implements State {
    @Override
    public void enter(com.pocketempire.entities.Unit unit) {
        System.out.println(unit.getId() + " is now in Chase state");
    }

    @Override
    public void update(com.pocketempire.entities.Unit unit, com.pocketempire.world.World world) {
        if (unit.getHp() < unit.getMaxHp() * 0.5) {
            unit.changeState(new FleeState(), UnitState.FLEEING);
            return;
        }

        com.pocketempire.entities.Unit target = null;
        int minDist = Integer.MAX_VALUE;

        for (var faction : world.getFactions()) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (com.pocketempire.entities.Unit enemy : faction.getUnits()) {
                if (enemy.getHp() <= 0) continue;
                int dist = com.pocketempire.world.HexUtils.getDistance(unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
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
                System.out.println(unit.getId() + " is chasing " + target.getId() + " (Distance: " + minDist + ")");
            }
        } else {
            unit.changeState(new IdleState(), UnitState.IDLE);
        }
    }

    @Override
    public void exit(com.pocketempire.entities.Unit unit) {}
}

