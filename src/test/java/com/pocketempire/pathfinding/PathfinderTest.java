package com.pocketempire.pathfinding;

import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.tiles.TileFactory;
import com.pocketempire.tiles.TileType;
import com.pocketempire.units.UnitFactory;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.Map;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PathfinderTest {

    private World createWorld(TileType[][] grid) {
        int height = grid.length;
        int width = grid[0].length;
        Tile[][] tiles = new Tile[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int q = col - (row - (row & 1)) / 2;
                int r = row;
                tiles[col][row] = TileFactory.create(q, r, grid[row][col]);
            }
        }
        Map map = new Map(width, height, tiles);
        Faction faction = new Faction(1, "Test", 0xFF0000);
        DiplomacyManager dm = new DiplomacyManager();
        dm.init(List.of(faction));
        return new World(map, List.of(faction), dm);
    }

    private Unit createUnit(World world, int q, int r, UnitType type) {
        Unit unit = UnitFactory.create(type, "u1", "Test", q, r, "1");
        world.getFactions().get(0).addUnit(unit);
        return unit;
    }

    @Test
    void findPath() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 0, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, 0, 0, 2, 1, unit);

        assertFalse(path.isEmpty());
        assertEquals(0, path.get(0).getQ());
        assertEquals(0, path.get(0).getR());
        assertEquals(2, path.get(path.size() - 1).getQ());
        assertEquals(1, path.get(path.size() - 1).getR());
    }

    @Test
    void findPath_longPath() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 0, 0, UnitType.ARCHER);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, 0, 0, 3, 1, unit);

        assertFalse(path.isEmpty());
        assertEquals(3, path.get(path.size() - 1).getQ());
        assertEquals(1, path.get(path.size() - 1).getR());
    }

    @Test
    void findPath_noPath() {
        // full wall of mountains at row 1
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.MOUNTAIN, TileType.MOUNTAIN, TileType.MOUNTAIN, TileType.MOUNTAIN, TileType.MOUNTAIN},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 1, 0, UnitType.LIGHT);

        // (1,0) -> (1,2): wall at row 1 blocks all paths
        List<Pathfinder.Node> path = Pathfinder.findPath(world, 1, 0, 1, 2, unit);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_noPath_startOutOfBounds() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 0, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, -10, -10, 1, 1, unit);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_noPath_endOutOfBounds() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 0, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, 0, 0, 100, 100, unit);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_noPath_bothOutOfBounds() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 0, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, -100, -100, 100, 100, unit);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_groundUnitBlockedByWater() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.SHALLOWS, TileType.SHALLOWS, TileType.SHALLOWS},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 1, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, 1, 0, 1, 2, unit);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_oceanAlwaysBlocks() {
        TileType[][] grid = {
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
            {TileType.OCEAN, TileType.OCEAN, TileType.OCEAN},
            {TileType.GRASS, TileType.GRASS, TileType.GRASS},
        };
        World world = createWorld(grid);
        Unit unit = createUnit(world, 1, 0, UnitType.LIGHT);

        List<Pathfinder.Node> path = Pathfinder.findPath(world, 1, 0, 1, 2, unit);

        assertTrue(path.isEmpty());
    }
}
