package com.wrathborn.world;

import com.wrathborn.tiles.TileType;

public class Tile {
    private int q;  // axial coordinate q
    private int r;  // axial coordinate r
    private TileType type;

    public Tile(int q, int r, TileType type) {
        this.q = q;
        this.r = r;
        this.type = type;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public TileType getType() {
        return type;
    }

    // Cube coordinates для обчислень
    public int cubeX() {
        return q;
    }

    public int cubeY() {
        return -q - r;
    }

    public int cubeZ() {
        return r;
    }
}