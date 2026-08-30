package com.pocketempire.player;

import com.pocketempire.config.TerrainConfigLoader;
import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.diplomacy.DiplomaticStatus;
import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.display.ConsoleRender;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.simulation.CombatResolver;
import com.pocketempire.simulation.UnitMover;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.UnitStats;
import com.pocketempire.units.UnitType;
import com.pocketempire.config.UnitConfigLoader;
import com.pocketempire.world.FogMap;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.VisibleWorld;
import com.pocketempire.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerController {
    private static final int MIN_CITY_DISTANCE = 2;
    private static final String HELP = """
            Commands:
              list                     - show all units and cities
              move <unitId> <q> <r>    - move unit
              attack <unitId> <q> <r>  - attack target
              settle <unitId>          - found city
              build <cityId> <TYPE>    - set single production
              queue <cityId> <TYPE>    - add to production queue
              qshow <cityId>           - show production queue
              qremove <cityId> <idx>   - remove from queue by index
              qclear <cityId>          - clear production queue
              inspect <q> <r>          - show tile/unit/city details
              map                      - redraw map
              help                     - show this help
              help terrain             - terrain bonuses
              help units               - unit stats
              debug                    - show hidden info
              1-9                      - select unit by number
              end                      - end turn
            """;

    private final World world;
    private final Map<Integer, FogMap> fogMaps;
    private final UnitMover unitMover = new UnitMover();
    private DiplomacyManager diplomacyManager;

    public PlayerController(World world, Map<Integer, FogMap> fogMaps) {
        this.world = world;
        this.fogMaps = fogMaps;
    }

    public void setDiplomacyManager(DiplomacyManager dm) {
        this.diplomacyManager = dm;
    }

    public void takeTurn(Faction faction, Scanner scanner) {
        printStatusBar(faction);
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
                    case "queue" -> handleQueue(faction, parts);
                    case "qshow" -> handleQueueShow(faction, parts);
                    case "qremove" -> handleQueueRemove(faction, parts);
                    case "qclear" -> handleQueueClear(faction, parts);
                    case "inspect" -> handleInspect(faction, parts);
                    case "help" -> handleHelp(faction, parts);
                    case "debug" -> handleDebug(faction);
                    case "1", "2", "3", "4", "5", "6", "7", "8", "9" -> handleQuickSelect(faction, cmd);
                    default -> System.out.println("Unknown command. Type 'help' for commands.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Coordinates must be integers.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printStatusBar(Faction faction) {
        int totalUnits = (int) faction.getUnits().stream().filter(Unit::isAlive).count();
        int militaryUnits = (int) faction.getUnits().stream()
                .filter(Unit::isAlive)
                .filter(u -> u.getUnitType() != UnitType.SETTLER && u.getUnitType() != UnitType.WORKER)
                .count();
        int settlerCount = (int) faction.getUnits().stream()
                .filter(Unit::isAlive)
                .filter(u -> u.getUnitType() == UnitType.SETTLER)
                .count();
        int cityCount = (int) faction.getCities().stream().filter(City::isAlive).count();
        int totalProduction = faction.getCities().stream()
                .filter(City::isAlive)
                .mapToInt(City::getEffectiveProduction)
                .sum();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s  Turn  Gold: %-6d  VP: %-4d              %n",
                faction.getName(), faction.getGold(), faction.getVictoryPoints()));
        sb.append(String.format("Cities: %-3d  Units: %-3d (military: %-3d, settlers: %-2d)  Prod/turn: %-3d  %n",
                cityCount, totalUnits, militaryUnits, settlerCount, totalProduction));
        System.out.println(sb);
    }

    private void handleQuickSelect(Faction faction, String cmd) {
        int index = Integer.parseInt(cmd) - 1;
        List<Unit> alive = faction.getUnits().stream().filter(Unit::isAlive).collect(Collectors.toList());
        if (index >= alive.size()) {
            System.out.println("No unit #" + (index + 1) + " (you have " + alive.size() + " units).");
            return;
        }
        Unit u = alive.get(index);
        System.out.printf("Selected: %-14s (%d,%d) HP:%d/%d MOV:%d/%d ATK:%d DEF:%d RNG:%d state:%s%n",
                u.getId(), u.getQ(), u.getR(), u.getHp(), u.getMaxHp(),
                u.getRemainingOD(), u.getMovement(), u.getAttack(), u.getDefense(), u.getRange(), u.getUnitState());
    }

    private void handleQueue(Faction faction, String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: queue <cityId> <TYPE> [TYPE...] (add one or more items)");
            printAvailableUnits(faction);
            return;
        }
        City city = findCity(faction, parts[1]);
        if (city == null) { System.out.println("No such city."); return; }

        for (int i = 2; i < parts.length; i++) {
            UnitType type;
            try {
                type = UnitType.valueOf(parts[i].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown unit type: " + parts[i]);
                printAvailableUnits(faction);
                return;
            }
            UnitStats stats = UnitConfigLoader.getConfig(type.name());
            if (stats == null) { System.out.println("No config for " + type); return; }
            if (stats.getRequiredTech() != null && !faction.getResearchedTechs().contains(stats.getRequiredTech())) {
                System.out.println(type + " requires tech " + stats.getRequiredTech() + " (not researched).");
                return;
            }
            city.enqueueProduction(type);
        }
        showQueue(city);
    }

    private void handleQueueShow(Faction faction, String[] parts) {
        if (parts.length != 2) { System.out.println("Usage: qshow <cityId>"); return; }
        City city = findCity(faction, parts[1]);
        if (city == null) { System.out.println("No such city."); return; }
        showQueue(city);
    }

    private void handleQueueRemove(Faction faction, String[] parts) {
        if (parts.length != 3) { System.out.println("Usage: qremove <cityId> <index>"); return; }
        City city = findCity(faction, parts[1]);
        if (city == null) { System.out.println("No such city."); return; }
        int idx;
        try { idx = Integer.parseInt(parts[2]); } catch (NumberFormatException e) { System.out.println("Index must be integer."); return; }
        if (city.removeFromQueue(idx)) {
            System.out.println("Removed item " + idx + " from queue.");
        } else {
            System.out.println("Invalid index.");
        }
        showQueue(city);
    }

    private void handleQueueClear(Faction faction, String[] parts) {
        if (parts.length != 2) { System.out.println("Usage: qclear <cityId>"); return; }
        City city = findCity(faction, parts[1]);
        if (city == null) { System.out.println("No such city."); return; }
        city.clearQueue();
        System.out.println("Queue cleared for " + city.getId() + ".");
    }

    private void showQueue(City city) {
        List<UnitType> queue = city.getQueueSnapshot();
        String current = city.getCurrentProductionType() == null ? "(none)" : city.getCurrentProductionType().name();
        System.out.println("  Current: " + current + "  Progress: " + city.getAccumulatedProduction());
        if (queue.isEmpty()) {
            System.out.println("  Queue: (empty)");
        } else {
            System.out.println("  Queue:");
            for (int i = 0; i < queue.size(); i++) {
                UnitStats s = UnitConfigLoader.getConfig(queue.get(i).name());
                System.out.printf("    [%d] %s (cost %d)%n", i, queue.get(i), s != null ? s.getCost() : '?');
            }
        }
    }

    private void handleInspect(Faction faction, String[] parts) {
        if (parts.length != 3) { System.out.println("Usage: inspect <q> <r>"); return; }
        int[] coords = new int[2];
        if (!tryParseInt(parts[1], coords) || !tryParseInt(parts[2], coords)) {
            System.out.println("Coordinates must be integers.");
            return;
        }
        int q = coords[0], r = coords[1];
        if (!world.getMap().isInBounds(q, r)) {
            System.out.println("Out of bounds.");
            return;
        }

        Tile tile = world.getMap().getTile(q, r);
        FogMap fog = fogMaps.get(faction.getId());
        boolean explored = fog.isExplored(q, r);
        boolean visible = fog.isVisible(q, r);

        System.out.println("Tile (" + q + "," + r + ")");
        if (!explored) {
            System.out.println("  (not explored)");
            return;
        }
        System.out.println("  Type: " + tile.getType()
                + "  moveCost:" + tile.getType().getMovementCost()
                + "  defendBonus:" + TerrainConfigLoader.getDefendBonus(tile.getType())
                + "  attackMod:" + TerrainConfigLoader.getAttackModifier(tile.getType())
                + "  visible:" + visible);

        if (!visible) {
            System.out.println("  (fogged — no unit/city info)");
            return;
        }

        World view = getVisibleWorld(faction);
        Unit unit = view.getAllUnits().stream()
                .filter(u -> u.isAlive() && u.getQ() == q && u.getR() == r)
                .findFirst().orElse(null);
        if (unit != null) {
            System.out.printf("  Unit: %s  type:%s  HP:%d/%d  ATK:%d  DEF:%d  MOV:%d/%d  RNG:%d  state:%s%n",
                    unit.getId(), unit.getUnitType(), unit.getHp(), unit.getMaxHp(),
                    unit.getAttack(), unit.getDefense(), unit.getRemainingOD(), unit.getMovement(),
                    unit.getRange(), unit.getUnitState());
            System.out.println("  Owner: " + unit.getFactionId());
        }

        City city = world.getFactions().stream()
                .flatMap(f -> f.getCities().stream())
                .filter(c -> c.isAlive() && c.getQ() == q && c.getR() == r)
                .findFirst().orElse(null);
        if (city != null) {
            String prod = city.getCurrentProductionType() == null ? "(none)" : city.getCurrentProductionType().name();
            System.out.printf("  City: %s  HP:%d/%d  pop:%d  production:%d  buildings:%d%n",
                    city.getName(), city.getHp(), city.getMaxHp(),
                    city.getPopulation(), city.getEffectiveProduction(), city.getBuildings().size());
            System.out.println("  Producing: " + prod + "  progress:" + city.getAccumulatedProduction());
            if (!city.getBuildings().isEmpty()) {
                System.out.println("  Buildings: " + city.getBuildings().stream()
                        .map(b -> b.getName()).collect(Collectors.joining(", ")));
            }
        }
    }

    private void handleHelp(Faction faction, String[] parts) {
        if (parts.length < 2) {
            System.out.println(HELP);
            return;
        }
        switch (parts[1].toLowerCase()) {
            case "terrain" -> printTerrainHelp();
            case "units" -> printUnitsHelp(faction);
            default -> System.out.println("Unknown help topic. Try: help, help terrain, help units");
        }
    }

    private void printTerrainHelp() {
        System.out.println("── Terrain Bonuses ──");
        for (TileType t : TileType.values()) {
            int def = TerrainConfigLoader.getDefendBonus(t);
            int atk = TerrainConfigLoader.getAttackModifier(t);
            System.out.printf("  %-12s move:%d  defend:%+d  atk:%+d  water:%b%n",
                    t.name(), t.getMovementCost(), def, atk, t.isWater());
        }
    }

    private void printUnitsHelp(Faction faction) {
        System.out.println("── Unit Stats ──");
        for (UnitType type : UnitType.values()) {
            UnitStats s = UnitConfigLoader.getConfig(type.name());
            if (s == null) continue;
            boolean unlocked = s.getRequiredTech() == null || faction.getResearchedTechs().contains(s.getRequiredTech());
            System.out.printf("  %-10s HP:%-3d ATK:%-3d DEF:%-3d MOV:%-3d RNG:%-3d cost:%-3d mvmt:%-7s%s%s%n",
                    type, s.getHp(), s.getAttack(), s.getDefense(), s.getMovement(), s.getRange(),
                    s.getCost(), s.getMovementType(),
                    s.getRequiredTech() != null ? "  tech:" + s.getRequiredTech() : "",
                    unlocked ? "" : "  [LOCKED]");
        }
    }

    private void handleDebug(Faction faction) {
        System.out.println("Debug Info");
        System.out.println("Factions:");
        for (Faction f : world.getFactions()) {
            long alive = f.getUnits().stream().filter(Unit::isAlive).count();
            System.out.printf("  %-12s id:%d  alive:%b  gold:%-6d  cities:%-3d  units:%-3d  VP:%-4d  wars:%d%n",
                    f.getName(), f.getId(), f.isAlive(), f.getGold(),
                    f.getCityCount(), alive, f.getVictoryPoints(), f.getActiveWarCount());
        }
        if (diplomacyManager != null) {
            System.out.println("Diplomacy (from " + faction.getName() + "):");
            for (Faction f : world.getFactions()) {
                if (f.getId() == faction.getId()) continue;
                DiplomaticStatus st = diplomacyManager.getStatus(faction.getId(), f.getId());
                int rep = diplomacyManager.getReputation(faction.getId(), f.getId());
                System.out.printf("  vs %-12s  status:%-8s  rep:%-4d%n", f.getName(), st, rep);
            }
        }
        System.out.println("Tech: " + faction.getResearchedTechs());
        System.out.println("War cooldowns: " + faction.getActiveWarCount());
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
        int n = 1;
        for (Unit u : faction.getUnits()) {
            if (!u.isAlive()) continue;
            System.out.printf("  [%d] %-14s %-8s (%d,%d) HP:%d/%d MOV:%d/%d ATK:%d DEF:%d RNG:%d state:%s%n",
                    n++, u.getId(), u.getUnitType(), u.getQ(), u.getR(), u.getHp(), u.getMaxHp(),
                    u.getRemainingOD(), u.getMovement(), u.getAttack(), u.getDefense(), u.getRange(), u.getUnitState());
        }
    }

    private void printCities(Faction faction) {
        System.out.println("Cities:");
        for (City c : faction.getCities()) {
            if (!c.isAlive()) continue;
            String producing = c.getCurrentProductionType() == null ? "-" : c.getCurrentProductionType().name();
            int queueSize = c.getQueueSnapshot().size();
            System.out.printf("  %-14s (%d,%d) HP:%d/%d Prod:%s Progress:%d Queue:%d%n",
                    c.getId(), c.getQ(), c.getR(), c.getHp(), c.getMaxHp(),
                    producing, c.getAccumulatedProduction(), queueSize);
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
        int q = coords[0], r = coords[1];
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
        int q = coords[0], r = coords[1];

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
}
