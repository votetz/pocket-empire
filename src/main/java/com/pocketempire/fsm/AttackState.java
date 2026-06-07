package com.pocketempire.fsm;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;
import com.pocketempire.simulation.CombatResolver;

import java.util.Comparator;

public class AttackState implements State {
    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        Unit enemy = world.getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(
                        unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);

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

        City targetCity = findNearestEnemyCity(unit, world);
        if (targetCity != null) {
            int dist = HexUtils.getDistance(
                    unit.getQ(), unit.getR(), targetCity.getQ(), targetCity.getR());
            if (dist <= unit.getRange()) {
                CombatResolver.resolveCityAttack(unit, targetCity);
                if (!targetCity.isAlive()) {
                    System.out.println(targetCity.getName() + " has been destroyed!");
                }
            }
        }

        unit.changeState(new IdleState(), UnitState.IDLE);
    }

    private City findNearestEnemyCity(Unit unit, World world) {
        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : world.getFactions()) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            for (City city : faction.getCities()) {
                if (!city.isAlive()) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = city;
                }
            }
        }
        return nearest;
    }

    @Override
    public void exit(Unit unit) {}
}