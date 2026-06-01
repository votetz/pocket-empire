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
        System.out.println("Wrathborn - Hexagonal Grid Test\n");

        int mapWidth = 50;
        int mapHeight = 15;
        Map map = MapGenerator.generateRandomMap(mapWidth, mapHeight);
        System.out.println("Map generated: " + mapWidth + "x" + mapHeight + " hexagonal grid\n");

        Faction faction1 = new Faction(1, "Red Tribe", 0xFF0000);
        faction1.setAI(false);
        
        Faction faction2 = new Faction(2, "Blue Tribe", 0x0000FF);
        faction2.setAI(true);

        Unit warrior1 = UnitFactory.create(UnitType.LIGHT, "warrior1", 5, 5, "1");
        Unit archer1 = UnitFactory.create(UnitType.ARCHER, "archer1", 6, 5, "1");
        faction1.addUnit(warrior1);
        faction1.addUnit(archer1);

        Unit warrior2 = UnitFactory.create(UnitType.LIGHT, "warrior2", 15, 10, "2");
        Unit heavy2 = UnitFactory.create(UnitType.HEAVY, "heavy2", 14, 10, "2");
        faction2.addUnit(warrior2);
        faction2.addUnit(heavy2);

        City city1 = new City("city1", 5, 6, "Red Capital", 100, 100, 5, 10, "1", "leader1", 10);
        City city2 = new City("city2", 15, 9, "Blue Capital", 100, 100, 5, 10, "2", "leader2", 10);
        faction1.addCity(city1);
        faction2.addCity(city2);

        System.out.println("Factions created:");
        System.out.println("  " + faction1.getName() + " - Units: " + faction1.getUnitCount() + ", Cities: " + faction1.getCityCount());
        System.out.println("  " + faction2.getName() + " - Units: " + faction2.getUnitCount() + ", Cities: " + faction2.getCityCount());
        System.out.println();

        List<Faction> factions = new ArrayList<>();
        factions.add(faction1);
        factions.add(faction2);
        World world = new World(map, factions);

        TurnManager turnManager = new TurnManager(factions, world);

        List<Unit> allUnits = new ArrayList<>();
        allUnits.addAll(faction1.getUnits());
        allUnits.addAll(faction2.getUnits());

        ConsoleRender renderer = new ConsoleRender(map, allUnits);

        System.out.println("Initial Map");
        renderer.render();
        System.out.println();

        System.out.println("Hex Distance Test");
        System.out.println("Distance between warrior1 (5,5) and warrior2 (15,10): " + 
            com.wrathborn.world.HexUtils.getDistance(5, 5, 15, 10) + " hexes");
        System.out.println("Distance between archer1 (6,5) and heavy2 (14,10): " + 
            com.wrathborn.world.HexUtils.getDistance(6, 5, 14, 10) + " hexes");
        System.out.println();

        System.out.println("Simulating 10 turns");
        for (int i = 0; i < 10; i++) {
            System.out.println("\nTurn " + turnManager.getCurrentTurn());
            System.out.println("Current faction: " + turnManager.getCurrentFaction().getName());
            
            turnManager.nextTurn();

            renderer.render();
            
            if (turnManager.isGameOver()) {
                System.out.println("\nGAME OVER");
                System.out.println("Winner: " + turnManager.getWinner().getName());
                break;
            }
        }

        System.out.println("\nTest Complete");
        System.out.println("Hexagonal grid system is working");
    }
}