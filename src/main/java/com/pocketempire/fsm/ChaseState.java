package com.pocketempire.fsm;

import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
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

        Unit target = world.findNearestEnemy(unit);

        if (target != null) {
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), target.getQ(), target.getR());
            if (dist <= 1) {
                unit.changeState(new AttackState(), UnitState.ATTACKING);
            } else if (unit.getRemainingOD() > 0) {
                List<Pathfinder.Node> path = Pathfinder.findPath(world, unit.getQ(), unit.getR(), target.getQ(), target.getR(), unit);
                if (path != null && path.size() > 1) {
                    Pathfinder.Node next = path.get(1);
                    int fromQ = unit.getQ(), fromR = unit.getR();
                    unit.setQ(next.getQ());
                    unit.setR(next.getR());
                    unit.spendOD(1);
                    GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
                }
            }
        } else {
            unit.changeState(new IdleState(), UnitState.IDLE);
        }
    }

    @Override
    public void exit(Unit unit) {}
}

