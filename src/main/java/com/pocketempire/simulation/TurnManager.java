package com.pocketempire.simulation;

import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.World;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.FogMap;
import com.pocketempire.fsm.UnitState;
import com.pocketempire.economy.EconomyManager;
import com.pocketempire.objective.VictoryManager;

import lombok.Getter;

import java.util.List;
import java.util.Map;

public class TurnManager {
    @Getter private int currentTurn;
    private int currentFactionIndex;
    private final List<Faction> factions;
    private final World world;
    private final Map<Integer, FogMap> fogMaps;
    private final EconomyManager economyManager = new EconomyManager();
    private final VictoryManager victoryManager;
    private final UnitMover unitMover = new UnitMover();
    private final AIProductionStrategy aiProductionStrategy = new AIProductionStrategy();
    private final UnitSpawner unitSpawner;

    public TurnManager(List<Faction> factions, World world, Map<Integer, FogMap> fogMaps) {
        this.factions = factions;
        this.world = world;
        this.fogMaps = fogMaps;
        this.currentTurn = 1;
        this.currentFactionIndex = 0;
        this.victoryManager = new VictoryManager(factions);
        this.unitSpawner = new UnitSpawner(world, fogMaps, aiProductionStrategy);
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
                ? new VisibleWorld(world, fogMaps.get(faction.getId()), String.valueOf(faction.getId()))
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
                unitMover.moveUnitTowardCity(unit, faction, aiWorld);
            } else if (unit.getRange() == 1 && unit.getUnitState() != UnitState.WANDER) {
                unitMover.moveUnitTowardEnemy(unit, faction, aiWorld);
            }
        }

        cleanDeadUnits();
        victoryManager.checkEliminationVictory(factions);
        if (victoryManager.isGameOver()) return;

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            city.update();
            unitSpawner.trySpawnUnit(city, faction);
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
