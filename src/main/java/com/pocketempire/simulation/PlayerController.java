package com.pocketempire.simulation;

import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.display.ConsoleRender;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.units.UnitStats;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.FogMap;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PlayerController {
    private static final int MIN_CITY_DISTANCE = 2;
    private static final String HELP = "Commands: list | move <unitId> <q> <r> | attack <unitId> <q> <r> | "
            + "settle <unitId> | build <cityId> <UNIT_TYPE> | map | help | end";

    private final World world;
    private final Map<Integer, FogMap> fogMaps;
    private final UnitMover unitMover = new UnitMover();

    public PlayerController(World world, Map<Integer, FogMap> fogMaps) {
        this.world = world;
        this.fogMaps = fogMaps;
    }

    public void takeTurn(Faction faction, Scanner scanner) {
        System.out.println("\n=== " + faction.getName() + "'s turn (gold: " + faction.getGold()
                + ", VP: " + faction.getVictoryPoints() + ") ===");
        renderMap(faction);
        printUnits(faction);
        printCities(faction);
        System.out.println(HELP);

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) return;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "end" -> { return; }
                    case "list" -> { printUnits(faction); printCities(faction); }
                    case "map" -> renderMap(faction);
                    case "move" -> handleMove(faction, parts);
                    case "attack" -> handleAttack(faction, parts);
                    case "settle" -> handleSettle(faction, parts);
                    case "build" -> handleBuild(faction, parts);
                    case "help" -> printHelp(faction);
                    default -> System.out.println("Unknown command. " + HELP);
                }
            } catch (NumberFormatException e) {
                System.out.println("Coordinates must be integers.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private VisibleWorld getVisibleWorld(Faction faction) {
        return new VisibleWorld(world, fogMaps.get(faction.getId()), String.valueOf(faction.getId()));
    }

    private void renderMap(Faction faction) {
        VisibleWorld visibleWorld = getVisibleWorld(faction);
        List<Unit> visibleUnits = visibleWorld.getAllUnits();
        List<City> allCities = new ArrayList<>();
        for (Faction f : world.getFactions()) {
            allCities.addAll(f.getCities());
        }
        FogMap fog = fogMaps.get(faction.getId());
        fog.update(faction);
        new ConsoleRender(world.getMap(), visibleUnits, allCities).render(fog);
    }

    private void printUnits(Faction faction) {
        System.out.println("Units:");
        for (Unit u : faction.getUnits()) {
            if (!u.isAlive()) continue;
            System.out.printf("  %-14s %-8s (%d,%d) HP:%d/%d MOV:%d/%d ATK:%d DEF:%d RNG:%d state:%s%n",
                    u.getId(), u.getUnitType(), u.getQ(), u.getR(), u.getHp(), u.getMaxHp(),
                    u.getRemainingOD(), u.getMovement(), u.getAttack(), u.getDefense(), u.getRange(), u.getUnitState());
        }
    }

    private void printCities(Faction faction) {
        System.out.println("Cities:");
        for (City c : faction.getCities()) {
            if (!c.isAlive()) continue;
            String producing = c.getCurrentProductionType() == null ? "-" : c.getCurrentProductionType().name();
            System.out.printf("  %-14s (%d,%d) HP:%d/%d Producing:%s Progress:%d%n",
                    c.getId(), c.getQ(), c.getR(), c.getHp(), c.getMaxHp(), producing, c.getAccumulatedProduction());
        }
    }

    private Unit findUnit(Faction faction, String id) {
        return faction.getUnits().stream()
                .filter(u -> u.isAlive() && u.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    private City findCity(Faction faction, String id) {
        return faction.getCities().stream()
                .filter(c -> c.isAlive() && c.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    private boolean tryParseInt(String s, int[] out) {
        try {
            out[0] = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void handleMove(Faction faction, String[] parts) {
        if (parts.length != 4) { System.out.println("Usage: move <unitId> <q> <r>"); return; }
        Unit unit = findUnit(faction, parts[1]);
        if (unit == null) { System.out.println("No such unit."); return; }
        if (unit.getRemainingOD() <= 0) { System.out.println(unit.getId() + " has no movement left this turn."); return; }

        int[] coords = new int[2];
        if (!tryParseInt(parts[2], coords) || !tryParseInt(parts[3], coords)) {
            System.out.println("Coordinates must be integers.");
            return;
        }
        int q = coords[0];
        int r = coords[1];
        if (!world.getMap().isInBounds(q, r)) {
            System.out.println("Coordinates out of map bounds.");
            return;
        }
        unitMover.moveUnitAlongPath(unit, world, q, r, faction);
        System.out.println(unit.getId() + " is now at (" + unit.getQ() + "," + unit.getR()
                + "), OD left: " + unit.getRemainingOD());
    }

    private void handleAttack(Faction faction, String[] parts) {
        if (parts.length != 4) { System.out.println("Usage: attack <unitId> <q> <r>"); return; }
        Unit attacker = findUnit(faction, parts[1]);
        if (attacker == null) { System.out.println("No such unit."); return; }

        int[] coords = new int[2];
        if (!tryParseInt(parts[2], coords) || !tryParseInt(parts[3], coords)) {
            System.out.println("Coordinates must be integers.");
            return;
        }
        int q = coords[0];
        int r = coords[1];

        int dist = HexUtils.getDistance(attacker.getQ(), attacker.getR(), q, r);
        if (dist > attacker.getRange()) {
            System.out.println("Target out of range (range " + attacker.getRange() + ", distance " + dist + ").");
            return;
        }

        VisibleWorld visibleWorld = getVisibleWorld(faction);
        Unit defender = visibleWorld.getAllUnits().stream()
                .filter(u -> u.isAlive() && u.getQ() == q && u.getR() == r)
                .findFirst().orElse(null);

        if (defender != null) {
            if (defender.getFactionId().equals(String.valueOf(faction.getId()))) {
                System.out.println("Cannot attack your own unit.");
                return;
            }
            Faction defenderFaction = findFactionById(defender.getFactionId());

            int defTerrainBonus = CombatResolver.calculateDefenderTerrainBonus(world, q, r, defenderFaction);
            int attackerTerrainMod = CombatResolver.calculateAttackerTerrainModifier(world, attacker.getQ(), attacker.getR());

            CombatResolver.resolveCombat(attacker, defender, defTerrainBonus, attackerTerrainMod, faction, defenderFaction);
            System.out.println("Attacked " + defender.getId() + " (HP now " + defender.getHp() + ")"
                    + (attacker.isAlive() ? "" : " — " + attacker.getId() + " died in the counter-attack"));
            return;
        }

        City city = world.getFactions().stream()
                .flatMap(f -> f.getCities().stream())
                .filter(c -> c.isAlive() && c.getQ() == q && c.getR() == r)
                .findFirst().orElse(null);

        if (city != null) {
            if (city.getFactionId().equals(String.valueOf(faction.getId()))) {
                System.out.println("Cannot attack your own city.");
                return;
            }
            CombatResolver.resolveCityAttack(attacker, city, world);
            System.out.println("Attacked city " + city.getName());
            return;
        }

        System.out.println("No target at (" + q + "," + r + ").");
    }

    private Faction findFactionById(String factionId) {
        return world.getFactions().stream()
                .filter(f -> String.valueOf(f.getId()).equals(factionId))
                .findFirst().orElse(null);
    }

    private void handleSettle(Faction faction, String[] parts) {
        if (parts.length != 2) { System.out.println("Usage: settle <unitId>"); return; }
        Unit unit = findUnit(faction, parts[1]);
        if (unit == null) { System.out.println("No such unit."); return; }
        if (unit.getUnitType() != UnitType.SETTLER) {
            System.out.println(unit.getId() + " is not a settler.");
            return;
        }
        if (!isValidCitySite(unit.getQ(), unit.getR(), unit)) {
            System.out.println("Cannot found a city here (too close to another city, or blocked terrain).");
            return;
        }

        String cityId = "city_" + unit.getId();
        String cityName = UnitNamesLoader.getRandomCityName();
        City city = new City(cityId, unit.getQ(), unit.getR(), cityName, 30, 30, 3, 10, unit.getFactionId(), unit.getName(), 2);
        faction.addCity(city);
        faction.removeUnit(unit);
        GameEventBus.getInstance().publish(new GameEvent.UnitDied(unit, null));
        GameEventBus.getInstance().publish(new GameEvent.CityFounded(city, unit));
        System.out.println("Founded " + cityName + " at (" + city.getQ() + "," + city.getR() + ").");
    }

    private boolean isValidCitySite(int q, int r, Unit unit) {
        if (!world.getMap().isInBounds(q, r)) return false;
        var tile = world.getMap().getTile(q, r);
        if (tile == null || tile.getType().isBlocksMovement()) return false;
        if (world.isTileOccupied(q, r, unit)) return false;
        for (Faction f : world.getFactions()) {
            for (City c : f.getCities()) {
                if (c.isAlive() && HexUtils.getDistance(q, r, c.getQ(), c.getR()) < MIN_CITY_DISTANCE) return false;
            }
        }
        return true;
    }

    private void handleBuild(Faction faction, String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: build <cityId> <UNIT_TYPE>");
            printAvailableUnits(faction);
            return;
        }
        City city = findCity(faction, parts[1]);
        if (city == null) { System.out.println("No such city."); return; }

        UnitType type;
        try {
            type = UnitType.valueOf(parts[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown unit type.");
            printAvailableUnits(faction);
            return;
        }

        UnitStats stats = UnitConfigLoader.getConfig(type.name());
        if (stats.getRequiredTech() != null && !faction.getResearchedTechs().contains(stats.getRequiredTech())) {
            System.out.println(type + " requires tech " + stats.getRequiredTech() + " (not yet researched).");
            return;
        }

        city.setCurrentProductionType(type);
        System.out.println(city.getId() + " is now producing " + type + " (cost " + stats.getCost() + ").");
    }

    private void printAvailableUnits(Faction faction) {
        System.out.println("Available unit types:");
        for (UnitType type : UnitType.values()) {
            UnitStats stats = UnitConfigLoader.getConfig(type.name());
            if (stats == null) continue;
            boolean unlocked = stats.getRequiredTech() == null || faction.getResearchedTechs().contains(stats.getRequiredTech());
            System.out.printf("  %-10s cost:%d%s%n", type, stats.getCost(),
                    unlocked ? "" : " (locked: needs " + stats.getRequiredTech() + ")");
        }
    }

    private void printHelp(Faction faction) {
        System.out.println(HELP);
        System.out.println();
        printAvailableUnits(faction);
    }
}
