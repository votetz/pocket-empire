package com.pocketempire.display;

import com.pocketempire.display.AnsiColor;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.Map;
import com.pocketempire.world.Tile;

import java.util.List;

public class ConsoleRender {
    private final Map map;
    private List<Unit> units;
    private List<City> cities;

    public ConsoleRender(Map map, List<Unit> units, List<City> cities) {
        this.map = map;
        this.units = units;
        this.cities = cities;
    }

    public void render() {
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                int q = col - (row - (row & 1)) / 2;
                int r = row;

                Tile tile = map.getTile(q, r);
                Unit unit = getUnitAt(col, row);
                City city = getCityAt(col, row);

                if (unit != null) {
                    System.out.print(colorizeUnit(unit));
                } else if (city != null) {
                    System.out.print(colorizeCity(city));
                } else {
                    System.out.print(colorize(tile));
                }
            }
            System.out.println();
        }
    }

    private City getCityAt(int col, int row) {
        int q = col - (row - (row & 1)) / 2;
        int r = row;
        return cities.stream()
                .filter(c -> c.getQ() == q && c.getR() == r)
                .findFirst()
                .orElse(null);
    }

    private Unit getUnitAt(int col, int row) {
        int q = col - (row - (row & 1)) / 2;
        int r = row;
        return units.stream()
                .filter(Unit::isAlive)
                .filter(u -> u.getQ() == q && u.getR() == r)
                .findFirst()
                .orElse(null);
    }

    private String colorizeCity(City city) {
        String color = switch (city.getFactionId()) {
            case "1" -> AnsiColor.BRIGHT_RED;
            case "2" -> AnsiColor.PURPLE;
            case "3" -> AnsiColor.ORANGE;
            default  -> AnsiColor.WHITE;
        };
        return color + "C" + AnsiColor.RESET;
    }

    private String colorizeUnit(Unit unit) {
        String color = switch (unit.getFactionId()) {
            case "1" -> AnsiColor.BRIGHT_RED;
            case "2" -> AnsiColor.PURPLE;
            case "3" -> AnsiColor.ORANGE;
            default  -> AnsiColor.WHITE;
        };
        String symbol = switch (unit.getUnitType()) {
            case LIGHT  -> "L";
            case ARCHER -> "A";
            case HEAVY  -> "H";
            case MAGE   -> "M";
            case SIEGE  -> "S";
            case GUARDIAN -> "G";
            case SETTLER -> "T";
            case WORKER -> "W";
        };
        return color + symbol + AnsiColor.RESET;
    }

    private String colorize(Tile tile) {
        String color = switch (tile.getType()) {
            case GRASS, PLAINS         -> AnsiColor.GREEN;
            case FOREST, JUNGLE, TAIGA -> AnsiColor.DARK_GREEN;
            case WATER, SWAMPS         -> AnsiColor.BLUE;
            case OCEAN                 -> AnsiColor.CYAN;
            case DESERT, SAVANNA       -> AnsiColor.YELLOW;
            case MOUNTAIN, CAVES       -> AnsiColor.GRAY;
            case TUNDRA                -> AnsiColor.WHITE;
        };
        return color + tile.getType().symbol + AnsiColor.RESET;
    }
}