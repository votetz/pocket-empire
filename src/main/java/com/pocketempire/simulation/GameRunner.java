package com.pocketempire.simulation;

import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.diplomacy.DiplomaticStatus;
import com.pocketempire.display.ConsoleRender;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.ConsoleLogger;
import com.pocketempire.world.FogMap;

import java.util.ArrayList;
import java.util.List;

public class GameRunner {
    private final GameSetup setup;
    private final TurnManager turnManager;
    private final ConsoleRender renderer;
    private final List<Unit> allUnits;
    private final List<City> allCities;

    public GameRunner(GameSetup setup) {
        this.setup = setup;
        setup.getFactions().get(0).setAI(false);
        this.turnManager = new TurnManager(setup.getFactions(), setup.getWorld(), setup.getFogMaps(), setup.getDiplomacyManager());
        this.allUnits = new ArrayList<>();
        this.allCities = new ArrayList<>();
        this.renderer = new ConsoleRender(setup.getMap(), allUnits, allCities);
        new ConsoleLogger();
    }

    public void run() {
        printFactions();
        renderInitialMap();
        gameLoop();
    }

    private void printFactions() {
        System.out.println("Factions created:");
        for (Faction faction : setup.getFactions()) {
            System.out.println("  " + faction.getName() + " - Units: " + faction.getUnitCount() + ", Cities: " + faction.getCityCount()
                    + (faction.isAI() ? "" : " (YOU)"));
        }
        System.out.println();
    }

    private void renderInitialMap() {
        System.out.println("Initial Map");
        Faction firstFaction = setup.getFactions().get(0);
        FogMap fog = setup.getFogMaps().get(firstFaction.getId());
        fog.update(firstFaction);
        renderer.render(fog);
        System.out.println();
    }

    private void gameLoop() {
        System.out.println("Simulating 150 turns");
        for (int i = 0; i < 150; i++) {
            Faction currentFaction = turnManager.getCurrentFaction();
            turnManager.nextTurn();

            updateEntities();
            renderCurrentFaction(currentFaction);

            if (turnManager.isGameOver()) {
                printRankings();
                printDiplomacy();
                break;
            }
        }
    }

    private void updateEntities() {
        allUnits.clear();
        allCities.clear();
        for (Faction faction : setup.getFactions()) {
            allUnits.addAll(faction.getUnits());
            allCities.addAll(faction.getCities());
        }
    }

    private void renderCurrentFaction(Faction faction) {
        FogMap fog = setup.getFogMaps().get(faction.getId());
        fog.update(faction);
        renderer.render(fog);
    }

    private void printRankings() {
        System.out.println("\nGame over!");
        int place = 1;
        for (Faction f : turnManager.getRankedFactions()) {
            System.out.println("  " + place + ". " + f.getName() + " — " + f.getVictoryPoints() + " VP"
                    + (f == turnManager.getWinner() ? " (winner)" : ""));
            place++;
        }
    }

    private void printDiplomacy() {
        DiplomacyManager dm = turnManager.getDiplomacyManager();
        List<Faction> factions = setup.getFactions();
        System.out.println("\nDiplomatic Relations:");

        for (int i = 0; i < factions.size(); i++) {
            for (int j = i + 1; j < factions.size(); j++) {
                Faction a = factions.get(i);
                Faction b = factions.get(j);
                DiplomaticStatus status = dm.getStatus(a.getId(), b.getId());
                int rep = dm.getReputation(a.getId(), b.getId());
                String icon = switch (status) {
                    case WAR -> "\u2694\uFE0F";
                    case NEUTRAL -> "\uD83D\uDD4A\uFE0F";
                    case ALLIED -> "\uD83E\uDD1D";
                    case DEVOTED -> "\u2764\uFE0F";
                };
                System.out.println("  " + icon + " " + a.getName() + " <-> " + b.getName() + ": " + status + " (" + rep + ")");
            }
        }
    }
}
