package com.wrathborn.world;

public class Map {
    private final int width;
    private final int height;
    private final Tile[][] tiles;

    public Map(int width, int height, Tile[][] tiles) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return tiles[x][y];
    }
}
