package com.wrathborn.tiles;

import com.wrathborn.world.Tile;

public class TileFactory {
    public static Tile create(int x, int y, TileType type) {
        return new Tile(x, y, type);
    }
}