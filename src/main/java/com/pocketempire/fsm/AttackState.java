package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;
import com.pocketempire.simulation.CombatResolver;



public class AttackState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
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

        City targetCity = world.findNearestEnemyCity(unit);
        if (targetCity != null) {
            int dist = HexUtils.getDistance(
                    unit.getQ(), unit.getR(), targetCity.getQ(), targetCity.getR());
            if (dist <= unit.getRange()) {
                CombatResolver.resolveCityAttack(unit, targetCity);
                if (!targetCity.isAlive()) {
                    GameEventBus.getInstance().publish(new GameEvent.CityDestroyed(targetCity, unit));
                }
            }
        }

        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    @Override
    public void exit(Unit unit) {}
}