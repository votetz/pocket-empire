package com.wrathborn;

import com.wrathborn.world.Map;
import com.wrathborn.world.Tile;
import com.wrathborn.world.World;
import com.wrathborn.world.MapGenerator;
import com.wrathborn.tiles.TileType;
import com.wrathborn.tiles.TileFactory;
import com.wrathborn.display.ConsoleRender;
import com.wrathborn.display.StatsDisplay;


public class Main{
    public static void main(String[] args) {
        int width = 100;
        int height = 20;

        Map map = MapGenerator.generateRandomMap(width, height);
        World world = new World(map);

        ConsoleRender renderer = new ConsoleRender(world.getMap());
        StatsDisplay stats = new StatsDisplay();

        renderer.render();
        stats.displayStats();
    }

}