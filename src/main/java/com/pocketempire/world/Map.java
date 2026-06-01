package com.pocketempire.world;

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

    public Tile getTileOffSet(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return null;
        return tiles[col][row];
    }

    public Tile getTile(int q , int r) {
        int col = q + (r - (r & 1)) / 2;
        int row = r;
        return getTileOffSet(col, row);
    }

    public boolean isInBounds(int q, int r) {
        int col = q + (r - (r & 1)) / 2;
        int row = r;
        return col >= 0 && col < width && row >= 0 && row < height;
    }
}
