package com.pocketempire;

import com.pocketempire.display.ConsoleRender;
import com.pocketempire.display.StatsDisplay;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.simulation.TurnManager;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.Map;
import com.pocketempire.world.MapGenerator;
import com.pocketempire.world.World;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Pocket Empire - Hexagonal Grid Test\n");

        int mapWidth = 50;
        int mapHeight = 15;
        Map map = MapGenerator.generateRandomMap(mapWidth, mapHeight);
        System.out.println("Map generated: " + mapWidth + "x" + mapHeight + " hexagonal grid\n");

        Faction faction1 = new Faction(1, "Red Tribe", 0xFF0000);
        faction1.setAI(true);
        
        Faction faction2 = new Faction(2, "Purple Tribe", 0x0000FF);
        faction2.setAI(true);

        Unit warrior1 = UnitFactory.create(UnitType.LIGHT, "warrior1", 5, 5, "1");
        Unit archer1 = UnitFactory.create(UnitType.ARCHER, "archer1", 6, 5, "1");
        faction1.addUnit(warrior1);
        faction1.addUnit(archer1);

        Unit warrior2 = UnitFactory.create(UnitType.LIGHT, "warrior2", 15, 10, "2");
        Unit heavy2 = UnitFactory.create(UnitType.HEAVY, "heavy2", 14, 10, "2");
        faction2.addUnit(warrior2);
        faction2.addUnit(heavy2);

        City city1 = new City("city1", 5, 6, "Red Capital", 100, 100, 5, 10, "1", "leader1", 3);
        City city2 = new City("city2", 15, 9, "Purple Capital", 100, 100, 5, 10, "2", "leader2", 3);
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

        List<City> allCities = new ArrayList<>();
        allCities.addAll(faction1.getCities());
        allCities.addAll(faction2.getCities());

        ConsoleRender renderer = new ConsoleRender(map, allUnits, allCities);

        System.out.println("Initial Map");
        renderer.render();
        System.out.println();

        System.out.println("Hex Distance Test");
        System.out.println("Distance between warrior1 (5,5) and warrior2 (15,10): " + 
            com.pocketempire.world.HexUtils.getDistance(5, 5, 15, 10) + " hexes");
        System.out.println("Distance between archer1 (6,5) and heavy2 (14,10): " + 
            com.pocketempire.world.HexUtils.getDistance(6, 5, 14, 10) + " hexes");
        System.out.println();

        System.out.println("Simulating 20 turns");
        for (int i = 0; i < 20; i++) {
            System.out.println("\nTurn " + turnManager.getCurrentTurn());
            System.out.println("Current faction: " + turnManager.getCurrentFaction().getName());
            
            turnManager.nextTurn();

            allUnits.clear();
            allUnits.addAll(faction1.getUnits());
            allUnits.addAll(faction2.getUnits());
            allCities.clear();
            allCities.addAll(faction1.getCities());
            allCities.addAll(faction2.getCities());
            renderer.render();
            
            if (turnManager.isGameOver()) {
                System.out.println("\nGAME OVER");
                System.out.println("Winner: " + turnManager.getWinner().getName());
                break;
            }
        }
    }
}