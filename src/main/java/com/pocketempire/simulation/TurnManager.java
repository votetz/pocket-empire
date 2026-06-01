package com.pocketempire.simulation;

import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.world.World;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.pathfinding.Pathfinder.Node;

import java.util.List;

public class TurnManager {
    private int currentTurn;
    private int currentFactionIndex;
    private List<Faction> factions;
    private World world;

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
                int dist = com.pocketempire.world.HexUtils.getDistance(
                        unit.getQ(), unit.getR(), enemy.getQ(), enemy.getR());
                if (dist < minDist) {
                    minDist = dist;
                    target = enemy;
                }
            }
        }

        if (target == null) return;

        // find path to target
        List<Node> path = Pathfinder.findPath(
                world,
                unit.getQ(), unit.getR(),
                target.getQ(), target.getR(),
                unit
        );

        // make unit move to the first node in the path
        if (path.size() > 1) {
            Node next = path.get(1);
            int dq = next.getQ() - unit.getQ();
            int dr = next.getR() - unit.getR();
            unit.move(dq, dr);
            System.out.println(unit.getId() + " moved to (" + unit.getQ() + "," + unit.getR() + ")");
        }
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
            unit.update();

            if (faction.isAI()) {
                unit.updateAI(world);
            }

            moveUnitTowardEnemy(unit, faction);
            unit.getCurrentState().update(unit, world.getAllUnits());
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