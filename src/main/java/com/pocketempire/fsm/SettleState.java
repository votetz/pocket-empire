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
    private static final int MIN_CITY_DISTANCE = 3;
    private int targetQ = -1, targetR = -1;

    @Override
    public void enter(Unit unit) {}

    @Override
    public void update(Unit unit, World world) {
        if (isValidSite(unit.getQ(), unit.getR(), unit, world)) {
            settle(unit, world);
            return;
        }

        if (targetQ < 0 || !isValidSite(targetQ, targetR, unit, world)) {
            int[] site = findNearestValidSite(unit, world);
            if (site == null) return;
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

    private boolean isValidSite(int q, int r, Unit unit, World world) {
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

    private int[] findNearestValidSite(Unit unit, World world) {
        int bestQ = -1, bestR = -1, bestDist = Integer.MAX_VALUE;
        int radius = 8;
        for (int dq = -radius; dq <= radius; dq++) {
            for (int dr = -radius; dr <= radius; dr++) {
                int q = unit.getQ() + dq, r = unit.getR() + dr;
                if (!isValidSite(q, r, unit, world)) continue;
                int dist = Math.abs(dq) + Math.abs(dr);
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
