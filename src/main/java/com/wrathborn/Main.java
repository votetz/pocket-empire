package com.wrathborn;

import com.wrathborn.display.ConsoleRender;
import com.wrathborn.display.StatsDisplay;
import com.wrathborn.entities.City;
import com.wrathborn.entities.Faction;
import com.wrathborn.entities.Unit;
import com.wrathborn.simulation.TurnManager;
import com.wrathborn.units.UnitFactory;
import com.wrathborn.units.UnitType;
import com.wrathborn.world.Map;
import com.wrathborn.world.MapGenerator;
import com.wrathborn.world.World;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // map
        int width = 100;
        int height = 20;
        Map map = MapGenerator.generateRandomMap(width, height);
        World world = new World(map);

        // faction
        Faction ironborn = new Faction(1, "Ironborn", 0xFF0000);
        Faction ashveil = new Faction(2, "Ashveil", 0x0000FF);

        // sity
        City city1 = new City("c1", 10, 5, "Ironhold", 100, 100, 500, 1000, "1", null, 2);
        City city2 = new City("c2", 50, 15, "Ashgate", 100, 100, 400, 1000, "2", null, 2);
        ironborn.addCity(city1);
        ashveil.addCity(city2);

        // unit
        ironborn.addUnit(UnitFactory.create(UnitType.HEAVY,  "u1", 10, 6, "1"));
        ironborn.addUnit(UnitFactory.create(UnitType.LIGHT,  "u2", 11, 6, "1"));
        ironborn.addUnit(UnitFactory.create(UnitType.ARCHER, "u3", 12, 6, "1"));
        ironborn.addUnit(UnitFactory.create(UnitType.MAGE,  "u7", 13, 6, "1"));

        ashveil.addUnit(UnitFactory.create(UnitType.HEAVY,  "u4", 50, 14, "2"));
        ashveil.addUnit(UnitFactory.create(UnitType.LIGHT,  "u5", 51, 14, "2"));
        ashveil.addUnit(UnitFactory.create(UnitType.ARCHER, "u6", 52, 14, "2"));

        // simulation
        List<Faction> factions = new ArrayList<>();
        factions.add(ironborn);
        factions.add(ashveil);
        TurnManager turnManager = new TurnManager(factions);


        // render
        ConsoleRender renderer = new ConsoleRender(world.getMap());
        StatsDisplay stats = new StatsDisplay();

        renderer.render();

        // game loop
        while (!turnManager.isGameOver()) {
            turnManager.nextTurn();
            System.out.println("Turn: " + turnManager.getCurrentTurn()
                    + " | " + ironborn.getName() + ": " + ironborn.getUnitCount() + " units"
                    + " | " + ashveil.getName() + ": " + ashveil.getUnitCount() + " units");
        }

        Faction winner = turnManager.getWinner();
        System.out.println("\nGAME OVER");
        System.out.println("Winner: " + (winner != null ? winner.getName() : "Draw"));

        stats.displayStats();
    }
}