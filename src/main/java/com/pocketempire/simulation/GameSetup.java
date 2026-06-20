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
import com.pocketempire.diplomacy.DiplomacyManager;

import lombok.Getter;
import java.util.ArrayList;
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
    @Getter private DiplomacyManager diplomacyManager;

    public GameSetup(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.factions = new ArrayList<>();
        this.fogMaps = new HashMap<>();
        this.diplomacyManager = new DiplomacyManager();
    }

    public void setup() {
        for (TileType type : TileType.values()) {
            type.loadBonuses();
        }

        int maxMapAttempts = 10;
        for (int i = 0; i < maxMapAttempts; i++) {
            map = MapGenerator.generateRandomMap(mapWidth, mapHeight);
            if (isMapPlayable(40)) break;
        }

        createFactions();
        diplomacyManager.init(factions);
        world = new World(map, factions, diplomacyManager);
        createFogMaps();
    }

    private boolean isMapPlayable(int minPassablePercent) {
        int total = mapWidth * mapHeight;
        int passable = 0;
        for (int q = 0; q < mapWidth; q++) {
            for (int r = 0; r < mapHeight; r++) {
                if (isvalidCityTile(q, r)) passable++;
            }
        }
        int percent = (passable * 100) / total;
        return percent >= minPassablePercent;
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
        int centerQ = mapWidth / 2;
        int centerR = mapHeight / 2;
        int radius = Math.min(mapWidth, mapHeight) / 3;
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 500; attempt++) {
            int[][] positions = new int[count][2];
            boolean valid = true;

            double angleOffset = rng.nextDouble(0, 2 * Math.PI);

            for (int i = 0; i < count; i++) {
                boolean placed = false;
                double baseAngle = angleOffset + (2 * Math.PI / count) * i;

                for (int try_ = 0; try_ < 100; try_++) {
                    double jitter = rng.nextDouble(-0.3, 0.3);
                    double angle = baseAngle + jitter;
                    int q = centerQ + (int) Math.round(Math.cos(angle) * radius);
                    int r = centerR + (int) Math.round(Math.sin(angle) * radius);

                    int col = q + (r - (r & 1)) / 2;
                    col = Math.max(1, Math.min(mapWidth - 2, col));
                    r = Math.max(1, Math.min(mapHeight - 2, r));
                    q = col - (r - (r & 1)) / 2;

                    if (!isvalidCityTile(q, r)) continue;
                    if (!hasEnoughExpansionRoom(q, r, 15)) continue;

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

                if (!placed) { valid = false; break; }
            }

            if (valid) return positions;
            radius = Math.max(3, radius - 1);
        }

        return generateFallbackPositions(count);
    }

    private boolean hasEnoughFreeNeighbours(int q, int r, int minFree) {
        int free = 0;
        for (int[] n : HexUtils.getNeighbors(q, r)) {
            if (isvalidCityTile(n[0], n[1])) free++;
        }
        return free >= minFree;
    }

    private boolean hasEnoughExpansionRoom(int q, int r, int minTiles) {
        int count = 0;
        for (int dq = -7; dq <= 7; dq++) {
            for (int dr = -7; dr <= 7; dr++) {
                if (dq == 0 && dr == 0) continue;
                if (isvalidCityTile(q + dq, r + dr)) count++;
            }
        }
        return count >= minTiles;
    }

    private int[][] generateFallbackPositions(int count) {
        int[][] fallback = new int[count][2];
        int centerCol = mapWidth / 2;
        int centerRow = mapHeight / 2;
        int spacing = Math.max(5, Math.min(mapWidth, mapHeight) / (count + 1));
        for (int i = 0; i < count; i++) {
            int col = centerCol + (int) Math.round(Math.cos(2 * Math.PI * i / count) * spacing);
            int row = centerRow + (int) Math.round(Math.sin(2 * Math.PI * i / count) * spacing);
            col = Math.max(1, Math.min(mapWidth - 2, col));
            row = Math.max(1, Math.min(mapHeight - 2, row));
            int q = col - (row - (row & 1)) / 2;
            int r = row;
            fallback[i] = findValidCityTile(q, r);
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

        for (int radius = 1; radius <= 20; radius++) {
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
