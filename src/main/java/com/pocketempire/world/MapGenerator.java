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

        double freq = 0.04;

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int q = col - (row - (row & 1)) / 2;
                int r = row;

                double elev = (elevNoise.noise(q * freq, r * freq) + 1) / 2;
                double moist = (moistNoise.noise(q * freq + 10, r * freq + 10) + 1) / 2;

                TileType type = resolveTile(elev, moist);
                tiles[col][row] = TileFactory.create(q, r, type);
            }
        }
        return new Map(width, height, tiles);
    }

    private static TileType resolveTile(double elev, double moist) {
        if (elev < 0.25) return TileType.OCEAN;
        if (elev < 0.30) return TileType.WATER;
        if (elev < 0.35) return TileType.DESERT;
        if (elev < 0.55) return moist < 0.4 ? TileType.DESERT : moist < 0.7 ? TileType.GRASS : TileType.FOREST;
        if (elev < 0.70) return moist < 0.3 ? TileType.PLAINS : moist < 0.6 ? TileType.FOREST : TileType.SWAMPS;
        if (elev < 0.80) return moist < 0.4 ? TileType.TUNDRA : TileType.TAIGA;
        return moist < 0.5 ? TileType.MOUNTAIN : TileType.CAVES;
    }
}
