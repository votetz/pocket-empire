package com.pocketempire.simulation;

import com.pocketempire.config.BuildingConfig;
import com.pocketempire.config.BuildingConfigLoader;
import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.tech.TechTree;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.MovementType;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitRole;
import com.pocketempire.units.UnitType;
import com.pocketempire.units.UnitStats;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.FogMap;

import java.util.Map;
import java.util.Random;

public class UnitSpawner {
    private final World world;
    private final Map<Integer, FogMap> fogMaps;
    private final AIProductionStrategy aiProductionStrategy;
    private final TechTree techTree = new TechTree();
    private int unitCounter;

    public UnitSpawner(World world, Map<Integer, FogMap> fogMaps, AIProductionStrategy aiProductionStrategy) {
        this.world = world;
        this.fogMaps = fogMaps;
        this.aiProductionStrategy = aiProductionStrategy;
        this.unitCounter = 0;
    }

    public void trySpawnUnit(City city, Faction faction, int currentTurn) {
        BuildingConfig buildingChoice = chooseBuildingForCity(city, faction);
        if (buildingChoice != null) {
            int buildingCost = buildingChoice.getProductionCost();
            if (faction.getConfig() != null && "Walls".equals(buildingChoice.getName())
                    && faction.getConfig().getWallCostReductionPercent() > 0) {
                buildingCost = buildingCost * (100 - faction.getConfig().getWallCostReductionPercent()) / 100;
            }
            if (city.getAccumulatedProduction() >= buildingCost) {
                city.addBuilding(buildingChoice);
                city.setAccumulatedProduction(city.getAccumulatedProduction() - buildingCost);
                GameEventBus.getInstance().publish(new GameEvent.BuildingBuilt(city, buildingChoice));
                return;
            }
        }

        int armyCap = calculateArmyCap(faction);
        boolean isCombatUnit = city.getCurrentProductionType() != UnitType.SETTLER && city.getCurrentProductionType() != UnitType.WORKER;
        if (isCombatUnit) {
            long combatCount = faction.getUnits().stream()
                .filter(u -> u.getUnitType() != UnitType.SETTLER && u.getUnitType() != UnitType.WORKER)
                .count();
            if (combatCount >= armyCap) {
                return;
            }
        }

        if (city.getCurrentProductionType() == null) {
            if (faction.isAI()) {
                FogMap fogMap = fogMaps.get(faction.getId());
                if (fogMap != null) {
                    World aiWorld = new VisibleWorld(world, fogMap, String.valueOf(faction.getId()));
                    aiProductionStrategy.chooseProductionForAI(city, faction, aiWorld, currentTurn);
                }
            }
            if (city.getCurrentProductionType() == null) return;
        }

        UnitStats unitStats = UnitConfigLoader.getConfig(city.getCurrentProductionType().name());
        String requiredTech = unitStats.getRequiredTech();
        if (requiredTech != null && !faction.getResearchedTechs().contains(requiredTech)) {
            city.setCurrentProductionType(null);
            return;
        }

        int cost = unitStats.getCost();
        if (faction.getConfig() != null) {
            if (city.getCurrentProductionType() == UnitType.LIGHT) {
                cost = Math.max(1, cost - faction.getConfig().getLightUnitCostReduction());
            }
            if (city.getCurrentProductionType() == UnitType.CATAPULT) {
                cost = Math.max(1, cost - faction.getConfig().getCatapultCostReduction());
            }
            if (city.getCurrentProductionType() == UnitType.MAGE) {
                cost = Math.max(1, cost - faction.getConfig().getMageCostReduction());
            }
        }
        boolean spawned = false;
        boolean isCiv = city.getCurrentProductionType() == UnitType.SETTLER || city.getCurrentProductionType() == UnitType.WORKER;

        while (city.getAccumulatedProduction() >= cost && (isCiv || faction.getGold() >= cost)) {
            int[] spawn = findSpawnTile(city, city.getCurrentProductionType());
            if (spawn == null) {
                city.setCurrentProductionType(null);
                break;
            }

            String unitId = city.getCurrentProductionType().name().toLowerCase()
                    + "_" + (++unitCounter);
            Unit unit = UnitFactory.create(city.getCurrentProductionType(), unitId, UnitNamesLoader.getRandomName(), spawn[0], spawn[1], city.getFactionId());
            if (faction.getConfig() != null && faction.getConfig().getMovementBonus() != 0) {
                unit.setMovementBonus(faction.getConfig().getMovementBonus());
            }
            if (city.getCurrentProductionType() == UnitType.HEAVY
                    && faction.getConfig() != null
                    && faction.getConfig().getHeavyHpBonus() != 0) {
                unit.setHp(unit.getHp() + faction.getConfig().getHeavyHpBonus());
                unit.setMaxHp(unit.getMaxHp() + faction.getConfig().getHeavyHpBonus());
            }
            if (city.getCurrentProductionType() == UnitType.MAGE) {
                unit.setAbilityType(aiProductionStrategy.chooseAbilityType(new Random()));
                var stats = UnitConfigLoader.getConfig(UnitType.MAGE.name());
                if (stats != null && stats.getRoleByAbility() != null) {
                    UnitRole mappedRole = stats.getRoleByAbility().get(unit.getAbilityType().name());
                    if (mappedRole != null) unit.setUnitRole(mappedRole);
                }
            }
            if (city.getAttackBonus() > 0) {
                unit.setAttack(unit.getAttack() + city.getAttackBonus());
            }
            faction.addUnit(unit);
            faction.spendGold(cost);
            city.setAccumulatedProduction(city.getAccumulatedProduction() - cost);
            GameEventBus.getInstance().publish(new GameEvent.UnitSpawned(unit));
            spawned = true;
            break;
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
                    case GROUND -> !type.isWater() && !type.isBlocksMovement();
                    default -> !type.isBlocksMovement();
                };
                if (!canSpawn) continue;
                return new int[]{nq, nr};
            }
        }
        return null;
    }

    private BuildingConfig chooseBuildingForCity(City city, Faction faction) {
        var researched = faction.getResearchedTechs();
        BuildingConfig walls = BuildingConfigLoader.getConfig("Walls");
        if (!city.hasBuilding("Walls") && techTree.isBuildingUnlocked(walls.getRequiredTech(), researched)) return walls;
        BuildingConfig market = BuildingConfigLoader.getConfig("Market");
        if (!city.hasBuilding("Market") && techTree.isBuildingUnlocked(market.getRequiredTech(), researched)) return market;
        BuildingConfig forge = BuildingConfigLoader.getConfig("Forge");
        if (!city.hasBuilding("Forge") && techTree.isBuildingUnlocked(forge.getRequiredTech(), researched)) return forge;
        if (city.hasBuilding("Forge") && !city.hasBuilding("Workshop")) return BuildingConfigLoader.getConfig("Workshop");
        BuildingConfig library = BuildingConfigLoader.getConfig("Library");
        if (!city.hasBuilding("Library") && techTree.isBuildingUnlocked(library.getRequiredTech(), researched)) return library;
        BuildingConfig granary = BuildingConfigLoader.getConfig("Granary");
        if (!city.hasBuilding("Granary") && techTree.isBuildingUnlocked(granary.getRequiredTech(), researched)) return granary;
        BuildingConfig barracks = BuildingConfigLoader.getConfig("Barracks");
        if (!city.hasBuilding("Barracks") && techTree.isBuildingUnlocked(barracks.getRequiredTech(), researched)) return barracks;
        BuildingConfig temple = BuildingConfigLoader.getConfig("Temple");
        if (!city.hasBuilding("Temple") && techTree.isBuildingUnlocked(temple.getRequiredTech(), researched)) return temple;
        return null;
    }

    private int calculateArmyCap(Faction faction) {
        int cities = faction.getCityCount();
        int totalBuildings = 0;
        for (City city : faction.getCities()) {
            if (city.isAlive()) {
                totalBuildings += city.getBuildings().size();
            }
        }
        return Math.max(4, (cities * 5) + (totalBuildings / 3));
    }
}
