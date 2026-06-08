package com.pocketempire;

import com.pocketempire.display.ConsoleRender;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.simulation.TurnManager;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.Map;
import com.pocketempire.world.MapGenerator;
import com.pocketempire.world.World;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.events.ConsoleLogger;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int mapWidth = 50;
        int mapHeight = 15;
        Map map = MapGenerator.generateRandomMap(mapWidth, mapHeight);

        Faction faction1 = new Faction(1, "Red Tribe", 0xFF0000);
        faction1.setAI(true);
        
        Faction faction2 = new Faction(2, "Purple Tribe", 0x0000FF);
        faction2.setAI(true);

        Faction faction3 = new Faction(3, "Orange Kingdom", 0x00FF00);
        faction3.setAI(true);

        Unit light1 = UnitFactory.create(UnitType.LIGHT, "light_1", UnitNamesLoader.getRandomName(), 5, 5, "1");
        Unit archer1 = UnitFactory.create(UnitType.ARCHER, "archer_1", UnitNamesLoader.getRandomName(), 6, 5, "1");
        faction1.addUnit(light1);
        faction1.addUnit(archer1);

        Unit light2 = UnitFactory.create(UnitType.LIGHT, "light_2", UnitNamesLoader.getRandomName(), 15, 8, "2");
        Unit archer2 = UnitFactory.create(UnitType.ARCHER, "archer_2", UnitNamesLoader.getRandomName(), 16, 9, "2");
        faction2.addUnit(light2);
        faction2.addUnit(archer2);

        City city1 = new City("city1", 5, 6, "Red Capital", 100, 100, 5, 10, "1", "leader1", 3);
        City city2 = new City("city2", 15, 9, "Purple Capital", 100, 100, 5, 10, "2", "leader2", 3);
        faction1.addCity(city1);
        faction2.addCity(city2);

        Unit light3 = UnitFactory.create(UnitType.LIGHT, "light_3", UnitNamesLoader.getRandomName(), 25, 5, "3");
        Unit archer3 = UnitFactory.create(UnitType.ARCHER, "archer_3", UnitNamesLoader.getRandomName(), 26, 5, "3");
        faction3.addUnit(light3);
        faction3.addUnit(archer3);

        City city3 = new City("city3", 25, 6, "Orange Capital", 100, 100, 5, 10, "3", "leader3", 3);
        faction3.addCity(city3);

        System.out.println("Factions created:");
        System.out.println("  " + faction1.getName() + " - Units: " + faction1.getUnitCount() + ", Cities: " + faction1.getCityCount());
        System.out.println("  " + faction2.getName() + " - Units: " + faction2.getUnitCount() + ", Cities: " + faction2.getCityCount());
        System.out.println("  " + faction3.getName() + " - Units: " + faction3.getUnitCount() + ", Cities: " + faction3.getCityCount());
        System.out.println();

        List<Faction> factions = new ArrayList<>();
        factions.add(faction1);
        factions.add(faction2);
        factions.add(faction3);
        World world = new World(map, factions);

        TurnManager turnManager = new TurnManager(factions, world);
        new ConsoleLogger();

        List<Unit> allUnits = new ArrayList<>();
        allUnits.addAll(faction1.getUnits());
        allUnits.addAll(faction2.getUnits());
        allUnits.addAll(faction3.getUnits());

        List<City> allCities = new ArrayList<>();
        allCities.addAll(faction1.getCities());
        allCities.addAll(faction2.getCities());
        allCities.addAll(faction3.getCities());

        ConsoleRender renderer = new ConsoleRender(map, allUnits, allCities);

        System.out.println("Initial Map");
        renderer.render();
        System.out.println();

        System.out.println("Simulating 50 turns");
        for (int i = 0; i < 150; i++) {
            turnManager.nextTurn();

            allUnits.clear();
            allUnits.addAll(faction1.getUnits());
            allUnits.addAll(faction2.getUnits());
            allUnits.addAll(faction3.getUnits());
            allCities.clear();
            allCities.addAll(faction1.getCities());
            allCities.addAll(faction2.getCities());
            allCities.addAll(faction3.getCities());
            renderer.render();
            
            if (turnManager.isGameOver()) {
                System.out.println("\nGame over!");
                System.out.println("Winner: " + turnManager.getWinner().getName());
                break;
            }
        }
    }
}