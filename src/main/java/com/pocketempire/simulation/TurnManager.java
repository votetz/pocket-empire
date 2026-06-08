package com.pocketempire.simulation;

import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.World;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.FogMap;
import com.pocketempire.world.Tile;
import com.pocketempire.world.HexUtils;
import com.pocketempire.fsm.UnitState;
import com.pocketempire.economy.EconomyManager;
import com.pocketempire.objective.VictoryManager;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.pathfinding.Pathfinder.Node;

import lombok.Getter;

import java.util.List;
import java.util.Random;

public class TurnManager {
    @Getter private int currentTurn;
    private int currentFactionIndex;
    private List<Faction> factions;
    private World world;
    private List<FogMap> fogMaps;
    private int unitCounter;
    private final EconomyManager economyManager = new EconomyManager();
    private final VictoryManager victoryManager;

    public TurnManager(List<Faction> factions, World world, List<FogMap> fogMaps) {
        this.factions = factions;
        this.world = world;
        this.fogMaps = fogMaps;
        this.currentTurn = 1;
        this.currentFactionIndex = 0;
        this.victoryManager = new VictoryManager(factions);
    }

    private void moveUnitTowardEnemy(Unit unit, Faction faction, World aiWorld) {
        Unit target = aiWorld.findNearestEnemy(unit);
        if (target == null) return;

        while (unit.getRemainingOD() > 0) {
            List<Node> path = Pathfinder.findPath(
                    aiWorld,
                    unit.getQ(), unit.getR(),
                    target.getQ(), target.getR(),
                    unit
            );

            if (path.size() <= 1) break;

            Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();

            Tile tile = aiWorld.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();

            if (unit.getRemainingOD() < cost) break;

            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.spendOD(cost);
            unit.move(dq, dr);
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    private void moveUnitTowardCity(Unit unit, Faction faction, World aiWorld) {
        City target = null;
        int minDist = Integer.MAX_VALUE;

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
            if (dist < minDist) {
                minDist = dist;
                target = city;
            }
        }

        if (target == null || minDist <= 1) return;

        while (unit.getRemainingOD() > 0) {
            List<Node> path = Pathfinder.findPath(
                    aiWorld,
                    unit.getQ(), unit.getR(),
                    target.getQ(), target.getR(),
                    unit
            );

            if (path.size() <= 1) break;

            Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();

            Tile tile = aiWorld.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();

            if (unit.getRemainingOD() < cost) break;

            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.spendOD(cost);
            unit.move(dq, dr);
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    public void nextTurn() {
        Faction current = factions.get(currentFactionIndex);
        if (current.isAlive()) {
            startFactionTurn(current);
            if (victoryManager.isGameOver()) return;
        }
        currentFactionIndex++;
        if(currentFactionIndex >= factions.size()) {
            currentFactionIndex = 0;
            currentTurn++;
            processGlobalTurnEffects();
            victoryManager.checkTimerVictory(currentTurn);
        }
    }

    private void startFactionTurn(Faction faction) {
        GameEventBus.getInstance().publish(new GameEvent.TurnStarted(currentTurn, faction));

        economyManager.processFactionEconomy(faction, world);

        World aiWorld = faction.isAI()
                ? new VisibleWorld(world, fogMaps.get(faction.getId() - 1), String.valueOf(faction.getId()))
                : world;

        for (Unit unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            unit.resetOD();
            unit.update();

            if (faction.isAI()) {
                unit.updateAI(aiWorld);
            }

            if (!unit.isAlive()) continue;

            if (unit.getUnitState() == UnitState.FLEEING) {
                moveUnitTowardCity(unit, faction, aiWorld);
            } else if (unit.getRange() == 1 && unit.getUnitState() != UnitState.WANDER) {
                moveUnitTowardEnemy(unit, faction, aiWorld);
            }
        }

        cleanDeadUnits();
        victoryManager.checkEliminationVictory(factions);
        if (victoryManager.isGameOver()) return;

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            city.update();
            trySpawnUnit(city, faction);
        }
    }

    private void cleanDeadUnits() {
        for (Faction faction : factions) {
            faction.getUnits().removeIf(unit -> !unit.isAlive());
            if (faction.getCityCount() == 0) {
                faction.setAlive(false);
            }
        }
    }

    private void trySpawnUnit(City city, Faction faction) {
        if (city.getCurrentProductionType() == null) {
            if (faction.isAI()) {
                World aiWorld = new VisibleWorld(world, fogMaps.get(faction.getId() - 1), String.valueOf(faction.getId()));
                chooseProductionForAI(city, faction, aiWorld);
            }
            if (city.getCurrentProductionType() == null) return;
        }

        int cost = UnitConfigLoader.getConfig(city.getCurrentProductionType().name()).getCost();
        boolean spawned = false;

        while (city.getAccumulatedProduction() >= cost && faction.getGold() >= cost) {
            int[] spawn = findSpawnTile(city);
            if (spawn == null) break;

            String unitId = city.getCurrentProductionType().name().toLowerCase()
                    + "_" + (++unitCounter);
            Unit unit = UnitFactory.create(city.getCurrentProductionType(), unitId, UnitNamesLoader.getRandomName(), spawn[0], spawn[1], city.getFactionId());
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

    private void chooseProductionForAI(City city, Faction faction, World aiWorld) {
        Random rng = new Random();

        if (rng.nextDouble() < 0.25 && faction.getCities().size() < 4) {
            city.setCurrentProductionType(
                    rng.nextBoolean() ? UnitType.SETTLER : UnitType.WORKER);
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

        if (nearest == null || minDist > 10) {
            UnitType[] combat = {UnitType.LIGHT, UnitType.ARCHER, UnitType.HEAVY,
                    UnitType.MAGE, UnitType.SIEGE, UnitType.GUARDIAN, UnitType.SCOUT};
            city.setCurrentProductionType(combat[rng.nextInt(combat.length)]);
            return;
        }

        if (nearest.getDefense() >= 4)      city.setCurrentProductionType(UnitType.MAGE);
        else if (nearest.getRange() >= 2)   city.setCurrentProductionType(UnitType.LIGHT);
        else                                city.setCurrentProductionType(UnitType.HEAVY);
    }

    private int[] findSpawnTile(City city) {
        for (int[] dir : HexUtils.DIRECTIONS) {
            int nq = city.getQ() + dir[0];
            int nr = city.getR() + dir[1];
            if (!world.getMap().isInBounds(nq, nr)) continue;
            Tile tile = world.getMap().getTile(nq, nr);
            if (tile == null || tile.getType().isBlocksMovement()) continue;
            if (world.isTileOccupied(nq, nr)) continue;
            return new int[]{nq, nr};
        }
        return null;
    }

    private void processGlobalTurnEffects() {
        // Implement global turn effects here
    }

    public Faction getCurrentFaction() {
        return factions.get(currentFactionIndex);
    }

    public boolean isGameOver() {
        return victoryManager.isGameOver();
    }

    public Faction getWinner() {
        return victoryManager.getWinner();
    }

    public List<Faction> getRankedFactions() {
        return victoryManager.getRankedFactions();
    }
}