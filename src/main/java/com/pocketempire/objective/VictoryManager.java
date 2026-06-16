package com.pocketempire.objective;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import lombok.Getter;

import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VictoryManager {
    private static final int TURN_LIMIT = 50;
    private static final int VP_PER_CITY_PER_TURN = 1;
    private static final int MAX_CITY_VP = 2;
    private static final int VP_PER_KILL = 5;
    private static final int VP_PER_CITY_FOUNDED = 10;
    private static final int VP_PER_TILE_IMPROVED = 3;

    private final Map<String, Faction> factionsById = new HashMap<>();
    @Getter private boolean gameOver = false;
    @Getter private Faction winner = null;

    public VictoryManager(List<Faction> factions) {
        for (Faction f : factions) {
            factionsById.put(String.valueOf(f.getId()), f);
        }
        subscribe();
    }

    private void subscribe() {
        GameEventBus bus = GameEventBus.getInstance();
        bus.subscribe(event -> {
            if (gameOver) return;
            switch (event) {
                case GameEvent.UnitDied(var unit, var killer) -> onUnitKilled(unit, killer);
                case GameEvent.CityFounded(var city, var settler) -> onCityFounded(city, settler);
                case GameEvent.TileImproved(var tile, var worker) -> onTileImproved(worker);
                case GameEvent.TurnStarted(var turn, var faction) -> onTurnStarted(faction);
                default -> {}
            }
        });
    }

    private void onUnitKilled(Unit victim, Unit killer) {
        if (killer == null) return;
        Faction killerFaction = factionsById.get(killer.getFactionId());
        if (killerFaction != null) {
            killerFaction.addVictoryPoints(VP_PER_KILL);
        }
    }

    private void onCityFounded(City city, Unit settler) {
        Faction faction = factionsById.get(settler.getFactionId());
        if (faction != null) {
            faction.addVictoryPoints(VP_PER_CITY_FOUNDED);
        }
    }

    private void onTileImproved(Unit worker) {
        Faction faction = factionsById.get(worker.getFactionId());
        if (faction != null) {
            faction.addVictoryPoints(VP_PER_TILE_IMPROVED);
        }
    }

    private void onTurnStarted(Faction faction) {
        int cities = Math.min(faction.getCityCount(), MAX_CITY_VP);
        faction.addVictoryPoints(cities * VP_PER_CITY_PER_TURN);
    }

    public void checkTimerVictory(int turn) {
        if (gameOver) return;
        if (turn < TURN_LIMIT) return;

        Faction best = null;
        int bestVP = -1;
        for (Faction f : factionsById.values()) {
            if (f.isAlive() && f.getVictoryPoints() > bestVP) {
                bestVP = f.getVictoryPoints();
                best = f;
            }
        }
        if (best != null) {
            endGame(best, "Turn limit reached (" + TURN_LIMIT + ")");
        }
    }

    public void checkEliminationVictory(List<Faction> factions) {
        if (gameOver) return;
        Faction alive = null;
        int aliveCount = 0;
        for (Faction f : factions) {
            if (f.isAlive()) {
                alive = f;
                aliveCount++;
            }
        }
        if (aliveCount <= 1) {
            if (alive != null) {
                endGame(alive, "Last faction standing");
            }
        }
    }

    public List<Faction> getRankedFactions() {
        return getSortedByVP();
    }

    private List<Faction> getSortedByVP() {
        return factionsById.values().stream()
                .sorted(Comparator.comparingInt(Faction::getVictoryPoints).reversed())
                .collect(Collectors.toList());
    }

    private void endGame(Faction winner, String reason) {
        this.gameOver = true;
        this.winner = winner;
        GameEventBus.getInstance().publish(new GameEvent.GameOver(winner, reason, getSortedByVP()));
    }
}
