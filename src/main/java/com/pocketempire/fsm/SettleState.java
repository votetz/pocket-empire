package com.pocketempire.fsm;

import com.pocketempire.config.UnitNamesLoader;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.pathfinding.Pathfinder;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.List;

public class SettleState implements State {
    private static final int MIN_CITY_DISTANCE = 2;
    private static final int STUCK_THRESHOLD = 5;
    private int targetQ = -1, targetR = -1;
    private int stuckTurns = 0;

    @Override
    public void enter(Unit unit) {
        stuckTurns = 0;
    }

    @Override
    public void update(Unit unit, World world) {
        if (isBasicValidSite(unit.getQ(), unit.getR(), unit, world)) {
            settle(unit, world);
            return;
        }

        if (targetQ < 0 || !isBasicValidSite(targetQ, targetR, unit, world)) {
            targetQ = -1;
            targetR = -1;
            boolean preferExpansion = stuckTurns < 3;
            int[] site = findNearestValidSite(unit, world, preferExpansion);
            if (site == null && preferExpansion) {
                site = findNearestValidSite(unit, world, false);
            }
            if (site == null) {
                stuckTurns++;
                if (stuckTurns >= STUCK_THRESHOLD) {
                    unit.takeDamage(unit.getHp() + 1);
                    GameEventBus.getInstance().publish(new GameEvent.UnitDied(unit, null));
                }
                return;
            }
            targetQ = site[0];
            targetR = site[1];
        }

        if (unit.getRemainingOD() <= 0) return;

        List<Pathfinder.Node> path = Pathfinder.findPath(
                world, unit.getQ(), unit.getR(), targetQ, targetR, unit);
        if (path != null && path.size() > 1) {
            Pathfinder.Node next = path.get(1);
            Tile tile = world.getMap().getTile(next.getQ(), next.getR());
            int cost = tile.getType().getMovementCost();
            if (unit.getRemainingOD() >= cost) {
                int fromQ = unit.getQ(), fromR = unit.getR();
                unit.spendOD(cost);
                unit.move(next.getQ() - fromQ, next.getR() - fromR);
                GameEventBus.getInstance().publish(new GameEvent.UnitMoved(unit, fromQ, fromR, unit.getQ(), unit.getR()));
                stuckTurns = 0;
            } else {
                targetQ = -1;
                targetR = -1;
                stuckTurns++;
                if (stuckTurns >= STUCK_THRESHOLD) {
                    unit.takeDamage(unit.getHp() + 1);
                    GameEventBus.getInstance().publish(new GameEvent.UnitDied(unit, null));
                }
            }
        } else {
            targetQ = -1;
            targetR = -1;
            stuckTurns++;
            if (stuckTurns >= STUCK_THRESHOLD) {
                unit.takeDamage(unit.getHp() + 1);
                GameEventBus.getInstance().publish(new GameEvent.UnitDied(unit, null));
            }
        }
    }

    private void settle(Unit unit, World world) {
        Faction faction = world.getFactions().stream()
                .filter(f -> String.valueOf(f.getId()).equals(unit.getFactionId()))
                .findFirst().orElse(null);
        if (faction == null) return;

        String cityId = "city_" + unit.getId();
        String cityName = UnitNamesLoader.getRandomCityName();
        City city = new City(cityId, unit.getQ(), unit.getR(), cityName, 30, 30,
                3, 10, unit.getFactionId(), unit.getName(), 2);
        faction.addCity(city);
        unit.takeDamage(unit.getHp() + 1);
        GameEventBus.getInstance().publish(new GameEvent.UnitDied(unit, null));
        GameEventBus.getInstance().publish(new GameEvent.CityFounded(city, unit));
    }

    private boolean isBasicValidSite(int q, int r, Unit unit, World world) {
        if (!world.getMap().isInBounds(q, r)) return false;
        Tile tile = world.getMap().getTile(q, r);
        if (tile == null || tile.getType().isBlocksMovement()) return false;
        if (world.isTileOccupied(q, r, unit)) return false;

        for (Faction f : world.getFactions()) {
            for (City c : f.getCities()) {
                if (c.isAlive() && HexUtils.getDistance(q, r, c.getQ(), c.getR()) < MIN_CITY_DISTANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasEnoughExpansionRoom(int centerQ, int centerR, World world) {
        int radius = 4;
        int passableCount = 0;
        for (int dq = -radius; dq <= radius; dq++) {
            for (int dr = Math.max(-radius, -dq - radius); dr <= Math.min(radius, -dq + radius); dr++) {
                int q = centerQ + dq, r = centerR + dr;
                if (!world.getMap().isInBounds(q, r)) continue;
                Tile tile = world.getMap().getTile(q, r);
                if (tile != null && !tile.getType().isBlocksMovement()) {
                    passableCount++;
                }
            }
        }
        return passableCount >= 8;
    }

    private int[] findNearestValidSite(Unit unit, World world, boolean requireExpansion) {
        int bestQ = -1, bestR = -1, bestDist = Integer.MAX_VALUE;
        int radius = 20;
        for (int dq = -radius; dq <= radius; dq++) {
            for (int dr = -radius; dr <= radius; dr++) {
                int q = unit.getQ() + dq, r = unit.getR() + dr;
                if (!isBasicValidSite(q, r, unit, world)) continue;
                if (requireExpansion && !hasEnoughExpansionRoom(q, r, world)) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), q, r);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestQ = q;
                    bestR = r;
                }
            }
        }
        return bestQ >= 0 ? new int[]{bestQ, bestR} : null;
    }

    @Override
    public void exit(Unit unit) {}
}
