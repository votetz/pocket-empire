package com.pocketempire.simulation;

import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.MageType;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AIProductionStrategy {

    public void chooseProductionForAI(City city, Faction faction, World aiWorld, int currentTurn) {
        Random rng = new Random();

        long workerCount = countUnitsByType(faction, UnitType.WORKER);
        long settlerCount = countUnitsByType(faction, UnitType.SETTLER);
        long scoutCount = countUnitsByType(faction, UnitType.SCOUT);

        long heavyCount = countUnitsByType(faction, UnitType.HEAVY) + countUnitsByType(faction, UnitType.GUARDIAN);
        long lightCount = countUnitsByType(faction, UnitType.LIGHT);
        long rangedCount = countUnitsByType(faction, UnitType.ARCHER) + countUnitsByType(faction, UnitType.MAGE) + countUnitsByType(faction, UnitType.SIEGE);

        long combatTotal = heavyCount + lightCount + rangedCount;

        int settlerCost = UnitConfigLoader.getConfig(UnitType.SETTLER.name()).getCost();
        if (faction.getCityCount() == 1 && currentTurn >= 10 && settlerCount == 0 && faction.getGold() >= settlerCost) {
            city.setCurrentProductionType(UnitType.SETTLER);
            return;
        }

        if (faction.getGold() < 5) {
            if (rng.nextDouble() < 0.5) {
                city.setCurrentProductionType(UnitType.LIGHT);
            } else {
                UnitType[] cheap = {UnitType.LIGHT, UnitType.ARCHER, UnitType.SCOUT};
                city.setCurrentProductionType(cheap[rng.nextInt(cheap.length)]);
            }
            return;
        }

        if (faction.getCityCount() < 5 && settlerCount < 2 && combatTotal >= 1 && rng.nextDouble() < 0.4) {
            city.setCurrentProductionType(UnitType.SETTLER);
            return;
        }

        if (workerCount < faction.getCityCount() && rng.nextDouble() < 0.35) {
            city.setCurrentProductionType(UnitType.WORKER);
            return;
        }

        if (scoutCount == 0) {
            city.setCurrentProductionType(UnitType.SCOUT);
            return;
        }

        long triremeCount = countUnitsByType(faction, UnitType.TRIREME);
        if (isNearWater(city, aiWorld) && triremeCount < 3 && rng.nextDouble() < 0.15) {
            city.setCurrentProductionType(UnitType.TRIREME);
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
            UnitType mostCommon = findMostCommonEnemyType(aiWorld, faction);
            if (mostCommon != null) {
                UnitType counter = getCounterUnit(mostCommon, rng);
                if (faction.getGold() >= UnitConfigLoader.getConfig(counter.name()).getCost()) {
                    city.setCurrentProductionType(counter);
                    return;
                }
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

    private boolean isNearWater(City city, World world) {
        for (int dq = -2; dq <= 2; dq++) {
            for (int dr = Math.max(-2, -dq - 2); dr <= Math.min(2, -dq + 2); dr++) {
                if (dq == 0 && dr == 0) continue;
                int nq = city.getQ() + dq;
                int nr = city.getR() + dr;
                if (world.getMap().isInBounds(nq, nr)) {
                    var tile = world.getMap().getTile(nq, nr);
                    if (tile != null && tile.getType().isWater()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public UnitType getCounterUnit(UnitType enemyType, Random rng) {
        return switch (enemyType) {
            case HEAVY, GUARDIAN -> rng.nextBoolean() ? UnitType.MAGE : UnitType.ARCHER;
            case LIGHT, SCOUT -> rng.nextBoolean() ? UnitType.HEAVY : UnitType.GUARDIAN;
            case ARCHER, MAGE -> rng.nextBoolean() ? UnitType.LIGHT : UnitType.SCOUT;
            case SIEGE -> UnitType.LIGHT;
            case TRIREME -> UnitType.DROMON;
            case SETTLER, WORKER -> UnitType.LIGHT;
            case DROMON -> UnitType.TRIREME;
        };
    }

    public UnitType findMostCommonEnemyType(World world, Faction faction) {
        Map<UnitType, Integer> counts = new HashMap<>();
        for (Unit enemy : world.getAllUnits()) {
            if (enemy.getFactionId().equals(String.valueOf(faction.getId()))) continue;
            if (!enemy.isAlive()) continue;
            if (enemy.getUnitType() == UnitType.SETTLER || enemy.getUnitType() == UnitType.WORKER) continue;
            counts.merge(enemy.getUnitType(), 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public long countUnitsByType(Faction faction, UnitType type) {
        return faction.getUnits().stream()
                .filter(u -> u.isAlive() && u.getUnitType() == type)
                .count();
    }

    public MageType chooseMageType(Random rng) {
        MageType[] types = MageType.values();
        return types[rng.nextInt(types.length)];
    }
}
