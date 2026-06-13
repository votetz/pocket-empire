package com.pocketempire.simulation;

import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.FogMap;
import com.pocketempire.world.Map;
import com.pocketempire.world.MapGenerator;
import com.pocketempire.world.World;

import lombok.Getter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSetup {
    private final int mapWidth;
    private final int mapHeight;

    @Getter private Map map;
    @Getter private World world;
    @Getter private List<Faction> factions;
    @Getter private java.util.Map<Integer, FogMap> fogMaps;

    public GameSetup(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.factions = new ArrayList<>();
        this.fogMaps = new HashMap<>();
    }

    public void setup() {
        map = MapGenerator.generateRandomMap(mapWidth, mapHeight);
        createFactions();
        world = new World(map, factions);
        createFogMaps();
    }

    private void createFactions() {
        Faction faction1 = new Faction(1, "Red Tribe", 0xFF0000);
        faction1.setAI(true);

        Faction faction2 = new Faction(2, "Purple Tribe", 0x0000FF);
        faction2.setAI(true);

        Faction faction3 = new Faction(3, "Orange Kingdom", 0x00FF00);
        faction3.setAI(true);

        createInitialUnitsAndCities(faction1, "1", 5, 5);
        createInitialUnitsAndCities(faction2, "2", 15, 8);
        createInitialUnitsAndCities(faction3, "3", 25, 5);

        factions.add(faction1);
        factions.add(faction2);
        factions.add(faction3);
    }

    private void createInitialUnitsAndCities(Faction faction, String factionId, int unitQ, int unitR) {
        Unit light = UnitFactory.create(UnitType.LIGHT, "light_" + factionId, UnitNamesLoader.getRandomName(), unitQ, unitR, factionId);
        Unit archer = UnitFactory.create(UnitType.ARCHER, "archer_" + factionId, UnitNamesLoader.getRandomName(), unitQ + 1, unitR, factionId);
        faction.addUnit(light);
        faction.addUnit(archer);

        City city = new City("city" + factionId, unitQ, unitR + 1, faction.getName() + " Capital", 100, 100, 5, 10, factionId, "leader" + factionId, 3);
        faction.addCity(city);
    }

    private void createFogMaps() {
        for (Faction faction : factions) {
            fogMaps.put(faction.getId(), new FogMap(map));
        }
    }
}
