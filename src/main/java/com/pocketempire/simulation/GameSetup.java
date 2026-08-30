package com.pocketempire.simulation;

import com.pocketempire.config.FactionConfig;
import com.pocketempire.config.FactionConfigLoader;
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
        List<String> allIds = FactionConfigLoader.getAll().stream()
                .map(FactionConfig::getId)
                .collect(java.util.stream.Collectors.toList());
        Collections.shuffle(allIds);
        String[] factionIds = allIds.subList(0, Math.min(3, allIds.size())).toArray(new String[0]);

        int[][] positions = generateStartPositions(factionIds.length, 12);

        for (int i = 0; i < factionIds.length; i++) {
            FactionConfig cfg = FactionConfigLoader.getConfig(factionIds[i]);
            int colorInt = Integer.parseInt(cfg.getColor().replace("#", ""), 16);
            Faction faction = new Faction(i + 1, cfg.getName(), colorInt);
            faction.setAI(true);
            faction.setConfig(cfg);
            faction.setGold(10 + cfg.getStartingGold());

            createInitialUnitsAndCities(faction, String.valueOf(i + 1), positions[i][0], positions[i][1]);
            factions.add(faction);
        }
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
            for (int dr = Math.max(-7, -dq - 7); dr <= Math.min(7, -dq + 7); dr++) {
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
            int[] pos = findValidCityTile(q, r);
            if (!hasEnoughExpansionRoom(pos[0], pos[1], 15)) {
                for (int dq = -10; dq <= 10; dq++) {
                    for (int dr = Math.max(-10, -dq - 10); dr <= Math.min(10, -dq + 10); dr++) {
                        if (dq == 0 && dr == 0) continue;
                        int nq = q + dq, nr = r + dr;
                        if (isvalidCityTile(nq, nr) && hasEnoughExpansionRoom(nq, nr, 15)) {
                            pos = new int[]{nq, nr};
                            break;
                        }
                    }
                    if (hasEnoughExpansionRoom(pos[0], pos[1], 15)) break;
                }
            }
            fallback[i] = pos;
        }
        return fallback;
    }

    private void createInitialUnitsAndCities(Faction faction, String factionId, int preferQ, int preferR) {
        int[] cityPos = findValidCityTile(preferQ, preferR);

        Unit scout = UnitFactory.createUnit(UnitType.SCOUT, "scout_" + factionId, UnitNamesLoader.getRandomName(), cityPos[0], cityPos[1], factionId);
        if (faction.getConfig() != null && faction.getConfig().getMovementBonus() != 0) {
            scout.setMovementBonus(faction.getConfig().getMovementBonus());
        }
        faction.addUnit(scout);

        City city = new City("city" + factionId, cityPos[0], cityPos[1], faction.getName() + " Capital", 50, 50, 5, 10, factionId, "leader" + factionId, 3);
        faction.addCity(city);
    }

    private int[] findValidCityTile(int preferQ, int preferR) {
        if (isvalidCityTile(preferQ, preferR) && hasEnoughExpansionRoom(preferQ, preferR, 15)) {
            return new int[]{preferQ, preferR};
        }

        for (int radius = 1; radius <= 20; radius++) {
            for (int dq = -radius; dq <= radius; dq++) {
                for (int dr = Math.max(-radius, -dq - radius); dr <= Math.min(radius, -dq + radius); dr++) {
                    if (dq == 0 && dr == 0) continue;
                    int nq = preferQ + dq;
                    int nr = preferR + dr;
                    if (isvalidCityTile(nq, nr) && hasEnoughExpansionRoom(nq, nr, 15)) {
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
