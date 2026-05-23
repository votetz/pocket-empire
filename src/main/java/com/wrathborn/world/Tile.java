package com.wrathborn.world;

import com.wrathborn.tiles.TileType;

public class Tile {
    private final int x;
    private final int y;
    private final TileType type;

    public Tile(int x, int y, TileType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TileType getType() {
        return type;
    }
}


