package com.pocketempire.simulation;

import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.entities.Building;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.MovementType;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.units.UnitStats;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.FogMap;

import java.util.List;
import java.util.Map;

public class UnitSpawner {
    private final World world;
    private final Map<Integer, FogMap> fogMaps;
    private final AIProductionStrategy aiProductionStrategy;
    private int unitCounter;

    public UnitSpawner(World world, Map<Integer, FogMap> fogMaps, AIProductionStrategy aiProductionStrategy) {
        this.world = world;
        this.fogMaps = fogMaps;
        this.aiProductionStrategy = aiProductionStrategy;
        this.unitCounter = 0;
    }

    public void trySpawnUnit(City city, Faction faction) {
        Building buildingChoice = chooseBuildingForCity(city);
        if (buildingChoice != null && city.getAccumulatedProduction() >= buildingChoice.getProductionCost()) {
            city.addBuilding(buildingChoice);
            city.setAccumulatedProduction(city.getAccumulatedProduction() - buildingChoice.getProductionCost());
            GameEventBus.getInstance().publish(new GameEvent.BuildingBuilt(city, buildingChoice));
            return;
        }

        if (city.getCurrentProductionType() == null) {
            if (faction.isAI()) {
                FogMap fogMap = fogMaps.get(faction.getId());
                if (fogMap != null) {
                    World aiWorld = new VisibleWorld(world, fogMap, String.valueOf(faction.getId()));
                    aiProductionStrategy.chooseProductionForAI(city, faction, aiWorld);
                }
            }
            if (city.getCurrentProductionType() == null) return;
        }

        int cost = UnitConfigLoader.getConfig(city.getCurrentProductionType().name()).getCost();
        boolean spawned = false;

        while (city.getAccumulatedProduction() >= cost && faction.getGold() >= cost) {
            int[] spawn = findSpawnTile(city, city.getCurrentProductionType());
            if (spawn == null) {
                city.setCurrentProductionType(null);
                break;
            }

            String unitId = city.getCurrentProductionType().name().toLowerCase()
                    + "_" + (++unitCounter);
            Unit unit = UnitFactory.create(city.getCurrentProductionType(), unitId, UnitNamesLoader.getRandomName(), spawn[0], spawn[1], city.getFactionId());
            if (city.getAttackBonus() > 0) {
                unit.setAttack(unit.getAttack() + city.getAttackBonus());
            }
            faction.addUnit(unit);
            faction.spendGold(cost);
            city.setAccumulatedProduction(city.getAccumulatedProduction() - cost);
            GameEventBus.getInstance().publish(new GameEvent.UnitSpawned(unit));
            spawned = true;
        }

        if (spawned) {
            city.setCurrentProductionType(null);
            city.setAccumulatedProduction(0);
        }
    }

    private int[] findSpawnTile(City city, UnitType unitType) {
        int[] result = findSpawnTileInRadius(city, 1, unitType);
        if (result != null) return result;
        return findSpawnTileInRadius(city, 2, unitType);
    }

    private int[] findSpawnTileInRadius(City city, int radius, UnitType unitType) {
        UnitStats stats = UnitConfigLoader.getConfig(unitType.name());
        MovementType movementType = stats.getMovementType();
        for (int dq = -radius; dq <= radius; dq++) {
            for (int dr = Math.max(-radius, -dq - radius); dr <= Math.min(radius, -dq + radius); dr++) {
                if (dq == 0 && dr == 0) continue;
                int nq = city.getQ() + dq;
                int nr = city.getR() + dr;
                if (!world.getMap().isInBounds(nq, nr)) continue;
                Tile tile = world.getMap().getTile(nq, nr);
                if (tile == null) continue;
                if (world.isTileOccupied(nq, nr)) continue;
                TileType type = tile.getType();
                boolean canSpawn = switch (movementType) {
                    case WATER -> type.isWater();
                    case GROUND -> !type.isWater() && !type.isBlocksMovement();
                    default -> !type.isBlocksMovement();
                };
                if (!canSpawn) continue;
                return new int[]{nq, nr};
            }
        }
        return null;
    }

    private Building chooseBuildingForCity(City city) {
        if (!city.hasBuilding(Building.WALLS)) return Building.WALLS;
        if (!city.hasBuilding(Building.MARKET)) return Building.MARKET;
        if (!city.hasBuilding(Building.FORGE)) return Building.FORGE;
        if (city.hasBuilding(Building.FORGE) && !city.hasBuilding(Building.WORKSHOP)) return Building.WORKSHOP;
        if (!city.hasBuilding(Building.BARRACKS)) return Building.BARRACKS;
        return null;
    }
}
