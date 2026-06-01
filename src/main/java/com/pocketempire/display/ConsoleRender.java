package com.pocketempire.display;

import com.pocketempire.display.AnsiColor;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.Map;
import com.pocketempire.world.Tile;

import java.util.List;

public class ConsoleRender {
    private final Map map;
    private List<Unit> units;

    public ConsoleRender(Map map, List<Unit> units) {
        this.map = map;
        this.units = units;
    }

    public void render() {
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                // offset to axial
                int q = col - (row - (row & 1)) / 2;
                int r = row;

                Tile tile = map.getTile(q, r);  // axial
                Unit unit = getUnitAt(col, row);  // offset

                if (unit != null) {
                    System.out.print(colorizeUnit(unit));
                } else {
                    System.out.print(colorize(tile));
                }
            }
            System.out.println();
        }
    }

    private Unit getUnitAt(int col, int row) {
        // offset to axial
        int q = col - (row - (row & 1)) / 2;
        int r = row;

        return units.stream()
                .filter(u -> u.getQ() == q && u.getR() == r)
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

    private String colorize(Tile tile) {
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