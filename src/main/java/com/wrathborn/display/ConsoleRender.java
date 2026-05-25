package com.wrathborn.display;

import com.wrathborn.display.AnsiColor;
import com.wrathborn.entities.Unit;
import com.wrathborn.world.Map;
import com.wrathborn.world.Tile;

import java.util.List;

public class ConsoleRender {
    private final Map map;
    private List<Unit> units;

    public ConsoleRender(Map map, List<Unit> units) {
        this.map = map;
        this.units = units;
    }

    public void render() {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTile(x, y);
                Unit unit = getUnitAt(x, y);
                if (unit != null) {
                    System.out.print(colorizeUnit(unit));
                } else {
                    System.out.print(colorize(tile, x, y));
                }
            }
            System.out.println();
        }
    }

    private Unit getUnitAt(int x, int y) {
        return units.stream()
                .filter(u -> u.getX() == x && u.getY() == y)
                .findFirst()
                .orElse(null);
    }

    private String colorizeUnit(Unit unit) {
        String color = switch (unit.getFactionId()) {
            case "1" -> AnsiColor.BRIGHT_RED;
            case "2" -> AnsiColor.CYAN;
            default  -> AnsiColor.WHITE;
        };
        return color + "U" + AnsiColor.RESET;
    }

    private String colorize(Tile tile, int x, int y) {
        String color = switch (tile.getType()) {
            case GRASS, PLAINS         -> AnsiColor.GREEN;
            case FOREST, JUNGLE, TAIGA -> AnsiColor.DARK_GREEN;
            case WATER, SWAMPS         -> AnsiColor.BLUE;
            case OCEAN                 -> AnsiColor.CYAN;
            case DESERT, SAVANNA       -> AnsiColor.YELLOW;
            case MOUNTAIN, CAVES       -> AnsiColor.GRAY;case TUNDRA                -> AnsiColor.WHITE;
        };
        return color + tile.getType().symbol + AnsiColor.RESET;
    }
}