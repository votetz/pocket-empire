package com.wrathborn;

import com.wrathborn.world.World;

public class Main{
    public static void main(String[] args) {
        com.wrathborn.world.Map map = createSampleMap(30, 10);
        World world = new World(map);

        com.wrathborn.display.ConsoleRender renderer = new com.wrathborn.display.ConsoleRender(world.getMap());
        com.wrathborn.display.StatsDisplay stats = new com.wrathborn.display.StatsDisplay();

        renderer.render();
        stats.displayStats();
    }

    private static com.wrathborn.world.Map createSampleMap(int width, int height) {
        com.wrathborn.world.Tile[][] tiles = new com.wrathborn.world.Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                com.wrathborn.tiles.TileType type = (x == 0 || x == width - 1 || y == 0 || y == height - 1)
                    ? com.wrathborn.tiles.TileType.WATER
                    : com.wrathborn.tiles.TileType.GRASS;
                tiles[x][y] = com.wrathborn.tiles.TileFactory.create(x, y, type);
            }
        }
        return new com.wrathborn.world.Map(width, height, tiles);
    }

}