package com.pocketempire.simulation;

import com.pocketempire.config.StatusEffectConfig;
import com.pocketempire.diplomacy.CasusBelli;
import com.pocketempire.diplomacy.CasusBelliManager;
import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.tech.TechTree;
import com.pocketempire.tech.TechConfigLoader;
import com.pocketempire.tech.TechnologyConfig;
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
import java.util.Random;

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
    private final TechTree techTree = new TechTree();
    private final DiplomacyManager diplomacyManager;
    private final CasusBelliManager casusBelliManager;
    private final Random rng = new Random();

    public TurnManager(List<Faction> factions, World world, Map<Integer, FogMap> fogMaps, DiplomacyManager diplomacyManager) {
        this.factions = factions;
        this.world = world;
        this.fogMaps = fogMaps;
        this.currentTurn = 1;
        this.currentFactionIndex = 0;
        this.victoryManager = new VictoryManager(factions);
        this.unitSpawner = new UnitSpawner(world, fogMaps, aiProductionStrategy);
        this.diplomacyManager = diplomacyManager;
        this.casusBelliManager = new CasusBelliManager();
    }

    public void nextTurn() {
        Faction current = factions.get(currentFactionIndex);
        if (current.isAlive()) {
            processFactionEffects(current);
            startFactionTurn(current);
            if (victoryManager.isGameOver()) return;
        }
        currentFactionIndex++;
        if (currentFactionIndex >= factions.size()) {
            currentFactionIndex = 0;
            currentTurn++;
            victoryManager.checkTimerVictory(currentTurn);
            diplomacyManager.tickCooldowns();
            casusBelliManager.tickCooldowns(currentTurn);
            evaluateWarDeclarations();
            evaluatePeace();
        }
    }

    private void evaluateWarDeclarations() {
        for (Faction a : factions) {
            if (!a.isAlive() || !a.isAI()) continue;
            for (Faction b : factions) {
                if (!b.isAlive() || a.getId() == b.getId()) continue;
                if (diplomacyManager.isHostile(a.getId(), b.getId())) continue;

                CasusBelli reason = casusBelliManager.findReason(a, b, world);
                if (reason != null) {
                    diplomacyManager.declareWar(a, b, currentTurn, reason.getId());
                    casusBelliManager.onWarDeclared(reason.getId(), a, b, currentTurn);
                }
            }
        }
    }

    private void evaluatePeace() {
        for (Faction a : factions) {
            if (!a.isAlive() || !a.isAI()) continue;
            for (Faction b : factions) {
                if (!b.isAlive() || a.getId() == b.getId()) continue;
                if (!diplomacyManager.isHostile(a.getId(), b.getId())) continue;

                int myPower = calculateMilitaryPower(a);
                int enemyPower = calculateMilitaryPower(b);
                int warDuration = diplomacyManager.getWarDuration(a.getId(), b.getId(), currentTurn);
                int citiesLost = diplomacyManager.getCitiesLostInWar(a.getId(), b.getId(), a.getCityCount());
                int reputation = diplomacyManager.getReputation(a.getId(), b.getId());

                boolean armyDestroyed = myPower < enemyPower * 0.2;
                boolean lostCities = citiesLost >= 2;
                boolean warExhaustion = warDuration > 15 && reputation > -30;

                if (armyDestroyed || lostCities || warExhaustion) {
                    diplomacyManager.makePeace(a, b);
                }
            }
        }
    }

    private int findMinCityDistance(Faction a, Faction b) {
        int minDist = Integer.MAX_VALUE;
        for (City ca : a.getCities()) {
            for (City cb : b.getCities()) {
                if (!ca.isAlive() || !cb.isAlive()) continue;
                int dist = com.pocketempire.world.HexUtils.getDistance(ca.getQ(), ca.getR(), cb.getQ(), cb.getR());
                if (dist < minDist) minDist = dist;
            }
        }
        return minDist;
    }

    private int calculateMilitaryPower(Faction faction) {
        int power = 0;
        for (Unit unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            power += unit.getAttack() + unit.getDefense() + unit.getHp();
        }
        return power;
    }

    private void startFactionTurn(Faction faction) {
        GameEventBus.getInstance().publish(new GameEvent.TurnStarted(currentTurn, faction));

        economyManager.processFactionEconomy(faction, world);

        processResearch(faction);

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
            } else if (unit.getRange() == 1 && unit.getUnitState() != UnitState.WANDER && unit.getUnitState() != UnitState.GATHERING) {
                unitMover.moveUnitTowardEnemy(unit, faction, aiWorld);
            }
        }

        cleanDeadUnits();
        victoryManager.checkEliminationVictory(factions);
        if (victoryManager.isGameOver()) return;

        for (City city : faction.getCities()) {
            if (!city.isAlive()) continue;
            city.update();
            unitSpawner.trySpawnUnit(city, faction, currentTurn);
        }
    }

    private void cleanDeadUnits() {
        for (Faction faction : factions) {
            faction.getUnits().removeIf(unit -> !unit.isAlive());
            faction.getCities().removeIf(city -> !city.isAlive());
            if (faction.getCityCount() == 0) {
                faction.setAlive(false);
            }
        }
    }

    private void processFactionEffects(Faction faction) {
        GameEventBus bus = GameEventBus.getInstance();
        for (Unit unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            java.util.List<java.util.Map.Entry<StatusEffectConfig, Integer>> effects =
                    new java.util.ArrayList<>(unit.getActiveEffects().entrySet());
            unit.tickEffects();
            for (java.util.Map.Entry<StatusEffectConfig, Integer> entry : effects) {
                if (entry.getKey().getTickDamage() > 0) {
                    bus.publish(new GameEvent.StatusTick(unit, entry.getKey(), entry.getKey().getTickDamage()));
                }
            }
        }
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

    public DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    private void processResearch(Faction faction) {
        int researchYield = faction.getCityCount();
        for (City city : faction.getCities()) {
            researchYield += city.getResearchBonus();
        }
        faction.addResearchPoints(researchYield);

        if (faction.getCurrentResearch() != null) {
            TechnologyConfig tech = TechConfigLoader.getConfig(faction.getCurrentResearch());
            if (tech != null && faction.getResearchProgress() >= tech.getCost()) {
                faction.getResearchedTechs().add(faction.getCurrentResearch());
                GameEventBus.getInstance().publish(new GameEvent.ResearchCompleted(faction, tech));
                faction.setCurrentResearch(null);
                faction.setResearchProgress(0);
            }
        }

        if (faction.isAI() && faction.getCurrentResearch() == null) {
            String next = aiProductionStrategy.chooseTech(faction, techTree);
            if (next != null) {
                faction.setCurrentResearch(next);
                faction.setResearchProgress(0);
            }
        }
    }
}