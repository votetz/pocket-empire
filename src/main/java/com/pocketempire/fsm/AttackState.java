package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;
import com.pocketempire.simulation.CombatResolver;
import com.pocketempire.pathfinding.Pathfinder;

public class AttackState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (unit.getUnitType() == UnitType.SIEGE) {
            City targetCity = world.findNearestEnemyCity(unit);
            if (targetCity != null) {
                int dist = HexUtils.getDistance(
                        unit.getQ(), unit.getR(), targetCity.getQ(), targetCity.getR());
                if (dist <= unit.getRange()) {
                    CombatResolver.resolveCityAttack(unit, targetCity);
                    if (!targetCity.isAlive()) {
                        GameEventBus.getInstance().publish(new GameEvent.CityDestroyed(targetCity, unit));
                    }
                    unit.changeState(new IdleState(), UnitState.IDLE);
                    return;
                } else {
                    moveToward(unit, targetCity.getQ(), targetCity.getR(), world);
                    return;
                }
            }
        }

        Unit enemy = world.findNearestEnemy(unit);
        if (enemy != null) {
            int dist = HexUtils.getDistance(
                    unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
            if (dist <= unit.getRange()) {
                CombatResolver.resolveCombat(unit, enemy,
                        world.getMap().getTile(enemy.getQ(), enemy.getR()).getType().getDefendBonus());
                unit.changeState(new IdleState(), UnitState.IDLE);
                return;
            } else {
                if (unit.getRange() > 1) {
                    unit.changeState(new SkirmishState(), UnitState.SKIRMISH);
                } else {
                    unit.changeState(new ChaseState(), UnitState.CHASE);
                }
                return;
            }
        }

        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    private void moveToward(Unit unit, int targetQ, int targetR, World world) {
        while (unit.getRemainingOD() > 0) {
            var path = Pathfinder.findPath(
                    world, unit.getQ(), unit.getR(), targetQ, targetR, unit);
            if (path == null || path.size() < 2) break;

            var next = path.get(1);
            int cost = world.getMap().getTile(next.getQ(), next.getR()).getType().getMovementCost();
            if (unit.getRemainingOD() < cost || world.isTileOccupied(next.getQ(), next.getR(), unit)) break;

            int fromQ = unit.getQ(), fromR = unit.getR();
            unit.spendOD(cost);
            unit.setQ(next.getQ());
            unit.setR(next.getR());
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));

            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), targetQ, targetR);
            if (dist <= unit.getRange()) break;
        }
        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    @Override
    public void exit(Unit unit) {}
}
