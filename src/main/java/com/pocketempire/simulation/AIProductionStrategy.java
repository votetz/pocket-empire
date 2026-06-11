package com.pocketempire.simulation;

import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.Random;

public class AIProductionStrategy {

    public void chooseProductionForAI(City city, Faction faction, World aiWorld) {
        Random rng = new Random();

        long workerCount = countUnitsByType(faction, UnitType.WORKER);
        long settlerCount = countUnitsByType(faction, UnitType.SETTLER);
        long scoutCount = countUnitsByType(faction, UnitType.SCOUT);

        long heavyCount = countUnitsByType(faction, UnitType.HEAVY) + countUnitsByType(faction, UnitType.GUARDIAN);
        long lightCount = countUnitsByType(faction, UnitType.LIGHT);
        long rangedCount = countUnitsByType(faction, UnitType.ARCHER) + countUnitsByType(faction, UnitType.MAGE) + countUnitsByType(faction, UnitType.SIEGE);

        long combatTotal = heavyCount + lightCount + rangedCount;

        if (faction.getGold() < 5) {
            if (rng.nextDouble() < 0.5) {
                city.setCurrentProductionType(UnitType.LIGHT);
            } else {
                UnitType[] cheap = {UnitType.LIGHT, UnitType.ARCHER, UnitType.SCOUT};
                city.setCurrentProductionType(cheap[rng.nextInt(cheap.length)]);
            }
            return;
        }

        if (workerCount < faction.getCityCount() && rng.nextDouble() < 0.35) {
            city.setCurrentProductionType(UnitType.WORKER);
            return;
        }

        if (faction.getCityCount() < 5 && settlerCount < 1 && combatTotal >= 2 && rng.nextDouble() < 0.2) {
            city.setCurrentProductionType(UnitType.SETTLER);
            return;
        }

        if (scoutCount == 0) {
            city.setCurrentProductionType(UnitType.SCOUT);
            return;
        }

        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Unit enemy : aiWorld.getAllUnits()) {
            if (enemy.getFactionId().equals(String.valueOf(faction.getId()))) continue;
            if (!enemy.isAlive()) continue;
            int dist = HexUtils.getDistance(city.getQ(), city.getR(), enemy.getQ(), enemy.getR());
            if (dist < minDist) { minDist = dist; nearest = enemy; }
        }

        if (nearest != null && minDist <= 10) {
            UnitType counter = getCounterUnit(nearest.getUnitType(), rng);
            if (faction.getGold() >= UnitConfigLoader.getConfig(counter.name()).getCost()) {
                city.setCurrentProductionType(counter);
                return;
            }
        }

        if (combatTotal == 0) {
            city.setCurrentProductionType(UnitType.LIGHT);
            return;
        }

        double frontlinePct = (double) heavyCount / combatTotal;
        double lightPct = (double) lightCount / combatTotal;
        double rangedPct = (double) rangedCount / combatTotal;

        double frontlineNeed = Math.max(0, 0.35 - frontlinePct);
        double lightNeed = Math.max(0, 0.35 - lightPct);
        double rangedNeed = Math.max(0, 0.30 - rangedPct);

        double totalNeed = frontlineNeed + lightNeed + rangedNeed;

        if (totalNeed <= 0) {
            UnitType[] combat = {UnitType.LIGHT, UnitType.ARCHER, UnitType.HEAVY,
                    UnitType.MAGE, UnitType.SIEGE, UnitType.GUARDIAN};
            city.setCurrentProductionType(combat[rng.nextInt(combat.length)]);
            return;
        }

        double roll = rng.nextDouble() * totalNeed;

        if (roll < frontlineNeed) {
            city.setCurrentProductionType(rng.nextBoolean() ? UnitType.HEAVY : UnitType.GUARDIAN);
        } else if (roll < frontlineNeed + lightNeed) {
            city.setCurrentProductionType(UnitType.LIGHT);
        } else {
            UnitType[] ranged = {UnitType.ARCHER, UnitType.MAGE, UnitType.SIEGE};
            city.setCurrentProductionType(ranged[rng.nextInt(ranged.length)]);
        }
    }

    public UnitType getCounterUnit(UnitType enemyType, Random rng) {
        return switch (enemyType) {
            case HEAVY, GUARDIAN -> rng.nextBoolean() ? UnitType.MAGE : UnitType.ARCHER;
            case LIGHT, SCOUT -> rng.nextBoolean() ? UnitType.HEAVY : UnitType.GUARDIAN;
            case ARCHER, MAGE -> rng.nextBoolean() ? UnitType.LIGHT : UnitType.SCOUT;
            case SIEGE -> UnitType.LIGHT;
            case SETTLER, WORKER -> UnitType.LIGHT;
        };
    }

    public long countUnitsByType(Faction faction, UnitType type) {
        return faction.getUnits().stream()
                .filter(u -> u.isAlive() && u.getUnitType() == type)
                .count();
    }
}
