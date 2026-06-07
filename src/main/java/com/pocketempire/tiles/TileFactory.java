package com.pocketempire.tiles;

import com.pocketempire.world.Tile;

public class TileFactory {
    public static Tile create(int x, int y, TileType type) {
        return Tile.builder().q(x).r(y).type(type).improved(false).build();
    }
}