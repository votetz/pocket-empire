package com.pocketempire.world;

import com.pocketempire.tiles.TileFactory;
import com.pocketempire.tiles.TileType;

import java.util.Random;

public class MapGenerator {
    public static Map generateRandomMap(int width, int height) {
        Tile[][] tiles = new Tile[width][height];
        Random random = new Random();

        // offset
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                // offset to axial for tile
                int q = col - (row - (row & 1)) / 2;
                int r = row;

                // ocean
                if (col == 0 || col == width - 1 || row == 0 || row == height - 1) {
                    tiles[col][row] = TileFactory.create(q, r, TileType.OCEAN);
                    continue;
                }

                int roll = random.nextInt(100);
                TileType type;
                if      (roll < 30) type = TileType.GRASS;
                else if (roll < 45) type = TileType.FOREST;
                else if (roll < 55) type = TileType.MOUNTAIN;
                else if (roll < 65) type = TileType.WATER;
                else if (roll < 75) type = TileType.DESERT;
                else if (roll < 82) type = TileType.PLAINS;
                else if (roll < 88) type = TileType.JUNGLE;
                else if (roll < 93) type = TileType.TAIGA;
                else if (roll < 96) type = TileType.TUNDRA;
                else if (roll < 98) type = TileType.SWAMPS;
                else                type = TileType.CAVES;

                tiles[col][row] = TileFactory.create(q, r, type);
            }
        }
        return new Map(width, height, tiles);
    }
}