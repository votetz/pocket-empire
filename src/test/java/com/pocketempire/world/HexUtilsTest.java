package com.pocketempire.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HexUtilsTest {

    @Test
    void getDistance_sameHex() {
        assertEquals(0, HexUtils.getDistance(0, 0, 0, 0));
        assertEquals(0, HexUtils.getDistance(5, 3, 5, 3));
    }

    @Test
    void getDistance_adjacent() {
        assertEquals(1, HexUtils.getDistance(0, 0, 1, 0));   // east
        assertEquals(1, HexUtils.getDistance(0, 0, 1, -1));  // northeast
        assertEquals(1, HexUtils.getDistance(0, 0, 0, -1));  // northwest
        assertEquals(1, HexUtils.getDistance(0, 0, -1, 0));  // west
        assertEquals(1, HexUtils.getDistance(0, 0, -1, 1));  // southwest
        assertEquals(1, HexUtils.getDistance(0, 0, 0, 1));   // southeast
    }

    @Test
    void getDistance_twoSteps() {
        // (2,0) → x=2,y=-2,z=0  to  (0,2) → x=0,y=-2,z=2
        // (|2| + |0| + |2|) / 2 = 2
        assertEquals(2, HexUtils.getDistance(2, 0, 0, 2));
    }

    @Test
    void getDistance_negativeCoords() {
        assertEquals(1, HexUtils.getDistance(-1, -1, 0, -1));
        assertEquals(3, HexUtils.getDistance(-3, 2, -1, 3));
    }

    @Test
    void getDistance_symmetric() {
        assertEquals(HexUtils.getDistance(1, 2, 4, 5),
                     HexUtils.getDistance(4, 5, 1, 2));
    }

    @Test
    void getNeighbors_returns6() {
        assertEquals(6, HexUtils.getNeighbors(0, 0).size());
    }

    @Test
    void getNeighbors_containsExpectedDirections() {
        var neighbors = HexUtils.getNeighbors(3, 4);
        var expected = new int[][]{
            {4, 4}, {4, 3}, {3, 3}, {2, 4}, {2, 5}, {3, 5}
        };

        for (int[] e : expected) {
            boolean found = neighbors.stream()
                .anyMatch(n -> n[0] == e[0] && n[1] == e[1]);
            assertTrue(found, "Missing neighbor (" + e[0] + "," + e[1] + ")");
        }
    }
}
