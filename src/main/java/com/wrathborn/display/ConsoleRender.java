package com.wrathborn.display;

import com.wrathborn.world.Map;
import com.wrathborn.world.Tile;

public class ConsoleRender {
    private final Map map;

    public ConsoleRender(Map map) {
        this.map = map;
    }

    public void render() {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTile(x, y);
                System.out.print(colorize(tile));
            }
            System.out.println();
        }
    }

    private String colorize(Tile tile) {
        String color = switch (tile.getType()) {
            case GRASS, PLAINS        -> AnsiColor.GREEN;
            case FOREST, JUNGLE, TAIGA -> AnsiColor.DARK_GREEN;
            case WATER, SWAMPS        -> AnsiColor.BLUE;
            case OCEAN                -> AnsiColor.CYAN;
            case DESERT, SAVANNA      -> AnsiColor.YELLOW;
            case MOUNTAIN, CAVES      -> AnsiColor.GRAY;
            case TUNDRA               -> AnsiColor.WHITE;
        };
        return color + tile.getType().symbol + AnsiColor.RESET;
    }
}