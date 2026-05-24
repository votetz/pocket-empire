package com.wrathborn.simulation;

import com.wrathborn.entities.Faction;
import com.wrathborn.entities.Unit;
import com.wrathborn.entities.City;
import java.util.List;

public class TurnManager {
    private int currentTurn;
    private int currentFactionIndex;
    private List<Faction> factions;

    public TurnManager(List<Faction> factions) {
        this.factions = factions;
        this.currentTurn = 1;
        this.currentFactionIndex = 0;
    }

    public void nextTurn() {
        currentFactionIndex++;
        if (currentFactionIndex >= factions.size()) {
            currentFactionIndex = 0;
            currentTurn++;
            processGlobalTurnEffects();
        }
        startFactionTurn(factions.get(currentFactionIndex));
    }

    private void startFactionTurn(Faction faction) {
        if (!faction.isAlive()) {
            nextTurn();
            return;
        }

        for (Unit unit : faction.getUnits()) {
            unit.restoreStamina(unit.getMaxStamina());
            unit.update();
        }

        for (City city : faction.getCities()) {
            city.update();
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

    private void processGlobalTurnEffects() {
        // Implement global turn effects here
    }

    public Faction getCurrentFaction() {
        return factions.get(currentFactionIndex);
    }

    public int getCurrentTurn() {
        return currentTurn;
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