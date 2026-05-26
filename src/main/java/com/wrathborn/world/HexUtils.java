package com.wrathborn.world;

import java.util.ArrayList;
import java.util.List;

public class HexUtils {
    public final static int[][] DIRECTIONS = {
            {+1, 0}, // east
            {+1, -1}, //northeast
            {0, -1}, // northwest
            {-1, 0}, // west
            {-1, +1}, // southwest
            {0, +1} // southeast
    };

    public static  int getDistance(int q1, int r1, int q2, int r2) {
        int x1 = q1;
        int z1 = r1;
        int y1 = -x1 - z1;

        int x2 = q2;
        int z2 = r2;
        int y2 = -x2 - z2;

        int distance = (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
        return distance;
    }
}
