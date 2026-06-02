package com.pocketempire.pathfinding;

import com.pocketempire.world.HexUtils;
import com.pocketempire.entities.Unit;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.*;
import lombok.Getter;

public class Pathfinder {
    private record Point(int q, int r) {}

    @Getter
    public static class Node implements Comparable<Node> {
        private final int q, r;
        private final double g, h, f;
        private final Node parent;

        public Node(int q, int r, double g, double h, Node parent) {
            this.q = q;
            this.r = r;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    public static List<Node> findPath(World world, int startQ, int startR, int targetQ, int targetR, Unit unit) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();

        Node startNode = new Node(startQ, startR, 0, HexUtils.getDistance(startQ, startR, targetQ, targetR), null);
        openSet.add(startNode);

        Node bestNode = startNode;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (closedSet.contains(new Point(current.q, current.r))) continue;
            closedSet.add(new Point(current.q, current.r));

            if (current.h < bestNode.h) {
                bestNode = current;
            }

            if (current.q == targetQ && current.r == targetR) {
                return buildPath(current);
            }

            if (current.g > unit.getSpeed()) continue;

            for (int[] neighbor : HexUtils.getNeighbors(current.q, current.r)) {
                int nq = neighbor[0], nr = neighbor[1];

                if (!world.getMap().isInBounds(nq, nr)) continue;
                if (closedSet.contains(new Point(nq, nr))) continue;

                Tile tile = world.getMap().getTile(nq, nr);
                if (tile == null || tile.getType().isBlocksMovement()) continue;

                if (isOccupied(world, nq, nr, unit)) continue;

                double stepCost = 1.0 / tile.getType().getMovementCost();
                double newG = current.g + stepCost;
                double h = HexUtils.getDistance(nq, nr, targetQ, targetR);

                openSet.add(new Node(nq, nr, newG, h, current));
            }
        }

        if (bestNode != startNode) {
            return buildPath(bestNode);
        }
        return Collections.emptyList();
    }

    private static boolean isOccupied(World world, int q, int r, Unit self) {
        for (Unit u : world.getAllUnits()) {
            if (u != self && u.isAlive() && u.getQ() == q && u.getR() == r) {
                return true;
            }
        }
        return false;
    }

    private static List<Node> buildPath(Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}