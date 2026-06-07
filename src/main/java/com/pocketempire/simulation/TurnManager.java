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
import com.pocketempire.world.Tile;
import com.pocketempire.world.HexUtils;
import com.pocketempire.fsm.UnitState;
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
    private int unitCounter;

    public TurnManager(List<Faction> factions, World world) {
        this.factions = factions;
        this.world = world;
        this.currentTurn = 1;
        this.currentFactionIndex = 0;
    }

    private void moveUnitTowardEnemy(Unit unit, Faction faction) {
        Unit target = null;
        int minDist = Integer.MAX_VALUE;

        for (Faction other : factions) {
            if (other == faction || !other.isAlive()) continue;
            for (Unit enemy : other.getUnits()) {
                if (!enemy.isAlive()) continue;
                int dist = com.pocketempire.world.HexUtils.getDistance(
                        unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
                if (dist < minDist) {
                    minDist = dist;
                    target = enemy;
                }
            }
        }

        if (target == null) return;

        while (unit.getRemainingOD() > 0) {
            List<Node> path = Pathfinder.findPath(
                    world,
                    unit.getQ(), unit.getR(),
                    target.getQ(), target.getR(),
                    unit
            );

            if (path.size() <= 1) break;

            Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();

            Tile tile = world.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();

            if (unit.getRemainingOD() < cost) break;

            int fromQ = unit.getQ();
            int fromR = unit.getR();
            unit.spendOD(cost);
            unit.move(dq, dr);
            GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
        }
    }

    private void moveUnitTowardCity(Unit unit, Faction faction) {
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
                    world,
                    unit.getQ(), unit.getR(),
                    target.getQ(), target.getR(),
                    unit
            );

            if (path.size() <= 1) break;

            Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();

            Tile tile = world.getMap().getTile(next.getQ(), next.getR());
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
        startFactionTurn(factions.get(currentFactionIndex));
        currentFactionIndex++;
        if(currentFactionIndex >= factions.size()) {
            currentFactionIndex = 0;
            currentTurn++;
            processGlobalTurnEffects();
        }
    }

    private void startFactionTurn(Faction faction) {
        if (!faction.isAlive()) {
            nextTurn();
            return;
        }

        GameEventBus.getInstance().publish(new GameEvent.TurnStarted(currentTurn, faction));

        for (Unit unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            unit.resetOD();
            unit.update();

            if (faction.isAI()) {
                unit.updateAI(world);
            }

            if (!unit.isAlive()) continue;

            if (unit.getUnitState() == UnitState.FLEEING) {
                moveUnitTowardCity(unit, faction);
            } else if (unit.getRange() == 1) {
                moveUnitTowardEnemy(unit, faction);
            }
        }

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            city.update();
            trySpawnUnit(city, faction);
        }

        cleanDeadUnits();
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
                chooseProductionForAI(city, faction);
            }
            if (city.getCurrentProductionType() == null) return;
        }

        int cost = UnitConfigLoader.getConfig(city.getCurrentProductionType().name()).getCost();

        while (city.getAccumulatedProduction() >= cost) {
            int[] spawn = findSpawnTile(city);
            if (spawn == null) break;

            String unitId = city.getCurrentProductionType().name().toLowerCase()
                    + "_" + (++unitCounter);
            Unit unit = UnitFactory.create(city.getCurrentProductionType(), unitId, UnitNamesLoader.getRandomName(), spawn[0], spawn[1], city.getFactionId());
            faction.addUnit(unit);
            city.setAccumulatedProduction(city.getAccumulatedProduction() - cost);
            GameEventBus.getInstance().publish(new GameEvent.UnitSpawned(unit));
        }
    }

    private void chooseProductionForAI(City city, Faction faction) {
        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Faction other : factions) {
            if (other == faction || !other.isAlive()) continue;
            for (Unit enemy : other.getUnits()) {
                if (!enemy.isAlive()) continue;
                int dist = HexUtils.getDistance(city.getQ(), city.getR(), enemy.getQ(), enemy.getR());
                if (dist < minDist) { minDist = dist; nearest = enemy; }
            }
        }

        if (nearest == null || minDist > 10) {
            UnitType[] combat = {UnitType.LIGHT, UnitType.ARCHER, UnitType.HEAVY, UnitType.MAGE, UnitType.SIEGE};
            city.setCurrentProductionType(combat[new Random().nextInt(combat.length)]);
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
            if (isTileOccupied(nq, nr)) continue;
            return new int[]{nq, nr};
        }
        return null;
    }

    private boolean isTileOccupied(int q, int r) {
        for (Faction f : factions) {
            if (!f.isAlive()) continue;
            for (Unit u : f.getUnits()) {
                if (u.isAlive() && u.getQ() == q && u.getR() == r) return true;
            }
        }
        return false;
    }

    private void processGlobalTurnEffects() {
        // Implement global turn effects here
    }

    public Faction getCurrentFaction() {
        return factions.get(currentFactionIndex);
    }

    public boolean isGameOver() {
        long aliveCount = factions.stream()
                .filter(Faction::isAlive)
                .count();
        return aliveCount <= 1;
    }

    public Faction getWinner() {
        return factions.stream()
                .filter(Faction::isAlive)
                .findFirst()
                .orElse(null);
    }
}