package com.pocketempire.tools;

import com.pocketempire.entities.Faction;
import com.pocketempire.simulation.GameSetup;
import com.pocketempire.simulation.TurnManager;

import java.util.HashMap;
import java.util.Map;

public class BatchSimulator {
    private static final int GAMES = 20;
    private static final int TURNS_PER_GAME = 150;

    public static void main(String[] args) {
        Map<String, int[]> stats = new HashMap<>();
        Map<String, int[]> placementCount = new HashMap<>();

        for (int game = 1; game <= GAMES; game++) {
            GameSetup setup = new GameSetup(35, 35);
            setup.setup();

            TurnManager tm = new TurnManager(
                    setup.getFactions(), setup.getWorld(),
                    setup.getFogMaps(), setup.getDiplomacyManager());

            for (int i = 0; i < TURNS_PER_GAME; i++) {
                tm.nextTurn();
                if (tm.isGameOver()) break;
            }

            var ranked = tm.getRankedFactions();
            for (int i = 0; i < ranked.size(); i++) {
                Faction f = ranked.get(i);
                String name = f.getName();
                stats.computeIfAbsent(name, k -> new int[2])[0]++;
                stats.get(name)[1] += f.getVictoryPoints();
                placementCount.computeIfAbsent(name, k -> new int[3])[i]++;
            }

            System.out.printf("Game %2d: %s %d VP | %s %d VP | %s %d VP%n",
                    game,
                    ranked.get(0).getName(), ranked.get(0).getVictoryPoints(),
                    ranked.get(1).getName(), ranked.get(1).getVictoryPoints(),
                    ranked.size() > 2 ? ranked.get(2).getName() : "-",
                    ranked.size() > 2 ? ranked.get(2).getVictoryPoints() : 0);
        }

        System.out.printf("%-16s %5s %7s %6s %6s %6s%n", "Faction", "Wins", "Avg VP", "1st", "2nd", "3rd");
        stats.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .forEach(e -> {
                    String name = e.getKey();
                    int wins = e.getValue()[0];
                    int totalVP = e.getValue()[1];
                    int[] places = placementCount.getOrDefault(name, new int[3]);
                    System.out.printf("%-16s %5d %7.1f %6d %6d %6d%n",
                            name, wins, (double) totalVP / GAMES,
                            places[0], places[1], places[2]);
                });
    }
}
