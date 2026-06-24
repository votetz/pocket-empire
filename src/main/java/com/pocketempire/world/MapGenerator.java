package com.pocketempire.world;

import com.pocketempire.tiles.TileFactory;
import com.pocketempire.tiles.TileType;

public class MapGenerator {
    public static Map generateRandomMap(int width, int height) {
        return generateRandomMap(width, height, System.nanoTime());
    }

    public static Map generateRandomMap(int width, int height, long seed) {
        Tile[][] tiles = new Tile[width][height];
        PerlinNoise elevNoise = new PerlinNoise((int) seed);
        PerlinNoise moistNoise = new PerlinNoise((int) (seed + 1));
        PerlinNoise riverNoise = new PerlinNoise((int) (seed + 2));

        double freq = 0.05;

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int q = col - (row - (row & 1)) / 2;
                int r = row;

                double elev = (elevNoise.noise(q * freq, r * freq) + 1) / 2;
                double moist = (moistNoise.noise(q * freq + 10, r * freq + 10) + 1) / 2;
                double river = (riverNoise.noise(q * 0.12, r * 0.12) + 1) / 2;

                TileType type = resolveTile(elev, moist, river);
                tiles[col][row] = TileFactory.create(q, r, type);
            }
        }
        return new Map(width, height, tiles);
    }

    private static TileType resolveTile(double elev, double moist, double river) {
        if (elev < 0.20 && river > 0.55) return TileType.SHALLOWS;
        if (elev < 0.30) return TileType.DESERT;
        if (elev < 0.45) return moist < 0.25 ? TileType.DESERT
                : moist < 0.5 ? TileType.PLAINS
                : moist < 0.75 ? TileType.GRASS
                : TileType.FOREST;
        if (elev < 0.60) return moist < 0.3 ? TileType.PLAINS
                : moist < 0.6 ? TileType.GRASS
                : TileType.FOREST;
        if (elev < 0.72) return moist < 0.4 ? TileType.FOREST
                : TileType.SWAMPS;
        if (elev < 0.82) return TileType.MOUNTAIN;
        return TileType.MOUNTAIN;
    }
}
