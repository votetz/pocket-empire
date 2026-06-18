package com.pocketempire.simulation;

import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.FogMap;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Map;
import com.pocketempire.world.MapGenerator;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
        for (TileType type : TileType.values()) {
            type.loadBonuses();
        }
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

        int[][] positions = generateStartPositions(3, 12);

        createInitialUnitsAndCities(faction1, "1", positions[0][0], positions[0][1]);
        createInitialUnitsAndCities(faction2, "2", positions[1][0], positions[1][1]);
        createInitialUnitsAndCities(faction3, "3", positions[2][0], positions[2][1]);

        factions.add(faction1);
        factions.add(faction2);
        factions.add(faction3);
    }

    private int[][] generateStartPositions(int count, int minDist) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int maxAttempts = 500;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int[][] positions = new int[count][2];
            boolean valid = true;

            for (int i = 0; i < count; i++) {
                boolean placed = false;

                for (int try_ = 0; try_ < 100; try_++) {
                    int q = rng.nextInt(1, mapWidth - 1);
                    int r = rng.nextInt(1, mapHeight - 1);

                    if (!isvalidCityTile(q, r)) continue;

                    boolean tooClose = false;
                    for (int j = 0; j < i; j++) {
                        if (HexUtils.getDistance(q, r, positions[j][0], positions[j][1]) < minDist) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) {
                        positions[i] = new int[]{q, r};
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    valid = false;
                    break;
                }
            }
            if (valid) return positions;
        }
        return generateFallbackPositions(count);
    }

    private int[][] generateFallbackPositions(int count) {
        int[][] fallback = new int[count][2];
        fallback[0] = findValidCityTile(2, 2);
        if (count > 1) fallback[1] = findValidCityTile(mapWidth / 2, mapHeight / 2);
        if (count > 2) fallback[2] = findValidCityTile(mapWidth - 3, mapHeight - 3);
        for (int i = 3; i < count; i++) {
            int q = (mapWidth / (count - 1)) * i;
            int r = (mapHeight / (count - 1)) * i;
            fallback[i] = findValidCityTile(
                Math.min(q, mapWidth - 2),
                Math.min(r, mapHeight - 2)
            );
        }
        return fallback;
    }

    private void createInitialUnitsAndCities(Faction faction, String factionId, int preferQ, int preferR) {
        int[] cityPos = findValidCityTile(preferQ, preferR);

        Unit scout = UnitFactory.create(UnitType.SCOUT, "scout_" + factionId, UnitNamesLoader.getRandomName(), cityPos[0], cityPos[1], factionId);
        faction.addUnit(scout);

        City city = new City("city" + factionId, cityPos[0], cityPos[1], faction.getName() + " Capital", 50, 50, 5, 10, factionId, "leader" + factionId, 3);
        faction.addCity(city);
    }

    private int[] findValidCityTile(int preferQ, int preferR) {
        if (isvalidCityTile(preferQ, preferR)) {
            return new int[]{preferQ, preferR};
        }

        for (int radius = 1; radius <= 10; radius++) {
            for (int dq = -radius; dq <= radius; dq++) {
                for (int dr = Math.max(-radius, -dq - radius); dr <= Math.min(radius, -dq + radius); dr++) {
                    if (dq == 0 && dr == 0) continue;
                    int nq = preferQ + dq;
                    int nr = preferR + dr;
                    if (isvalidCityTile(nq, nr)) {
                        return new int[]{nq, nr};
                    }
                }
            }
        }

        return new int[]{preferQ, preferR};
    }

    private boolean isvalidCityTile(int q, int r) {
        if (!map.isInBounds(q, r)) return false;
        Tile tile = map.getTile(q, r);
        if (tile == null) return false;
        TileType type = tile.getType();
        if (type.isWater()) return false;
        if (type.isBlocksMovement()) return false;
        return true;
    }

    private void createFogMaps() {
        for (Faction faction : factions) {
            fogMaps.put(faction.getId(), new FogMap(map));
        }
    }
}
