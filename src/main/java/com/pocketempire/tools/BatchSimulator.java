package com.pocketempire.tools;

import com.pocketempire.entities.Faction;
import com.pocketempire.simulation.GameSetup;
import com.pocketempire.simulation.TurnManager;

import java.util.HashMap;
import java.util.Map;

public class BatchSimulator {
    private static final int GAMES = 1000;
    private static final int TURNS_PER_GAME = 150;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Map<String, int[]> stats = new HashMap<>();

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
            Faction winner = ranked.isEmpty() ? null : ranked.get(0);
            if (winner != null) {
                stats.computeIfAbsent(winner.getName(), k -> new int[1])[0]++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        
        System.out.printf("Games: %d | Turns: %d | Map: 35x35%n", GAMES, TURNS_PER_GAME);
        System.out.printf("Time: %.1fs (%dms per game)%n", elapsed / 1000.0, elapsed / GAMES);

        stats.entrySet().stream()
                .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
                .forEach(e -> {
                    double pct = e.getValue()[0] * 100.0 / GAMES;
                    System.out.printf("%-16s %4d (%.1f%%)%n", e.getKey(), e.getValue()[0], pct);
                });
    }
}
