package com.pocketempire.world;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;

import java.util.LinkedList;
import java.util.Queue;

public class FogMap {
    private final int width;
    private final int height;
    private final Map map;
    private final boolean[][] visible;
    private final boolean[][] explored;
    private final boolean[] visited;

    public FogMap(Map map) {
        this.width = map.getWidth();
        this.height = map.getHeight();
        this.map = map;
        this.visible = new boolean[width][height];
        this.explored = new boolean[width][height];
        this.visited = new boolean[width * height];
    }

    public void update(Faction faction) {
        for (int col = 0; col < width; col++) {
            java.util.Arrays.fill(visible[col], false);
        }

        for (Unit u : faction.getUnits()) {
            if (u.isAlive()) {
                int range = u.getSightRange();
                if (faction.getConfig() != null) {
                    range += faction.getConfig().getSightBonus();
                }
                reveal(u.getQ(), u.getR(), range);
            }
        }
        for (City c : faction.getCities()) {
            if (c.isAlive()) {
                reveal(c.getQ(), c.getR(), 3);
            }
        }
    }

    private void reveal(int q, int r, int range) {
        java.util.Arrays.fill(visited, false);

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{q, r, 0});

        int startCol = q + (r - (r & 1)) / 2;
        if (startCol >= 0 && startCol < width && r >= 0 && r < height) {
            visited[startCol + r * width] = true;
        }

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cq = cur[0], cr = cur[1], dist = cur[2];

            int col = cq + (cr - (cr & 1)) / 2;
            if (col >= 0 && col < width && cr >= 0 && cr < height) {
                visible[col][cr] = true;
                explored[col][cr] = true;
            }

            if (dist >= range) continue;

            for (int[] dir : HexUtils.DIRECTIONS) {
                int nq = cq + dir[0];
                int nr = cr + dir[1];

                if (map.isInBounds(nq, nr)) {
                    int ncol = nq + (nr - (nr & 1)) / 2;
                    int flatIndex = ncol + nr * width;

                    if (!visited[flatIndex]) {
                        visited[flatIndex] = true;
                        queue.add(new int[]{nq, nr, dist + 1});
                    }
                }
            }
        }
    }

    public boolean isVisible(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return false;
        return visible[col][row];
    }

    public boolean isExplored(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return false;
        return explored[col][row];
    }
}
