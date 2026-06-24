package com.pocketempire.world;

import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;

import java.util.ArrayList;
import java.util.List;

public class VisibleWorld extends World {
    private final FogMap fogMap;
    private final String factionId;

    public VisibleWorld(World world, FogMap fogMap, String factionId) {
        super(world.getMap(), world.getFactions(), world.getDiplomacyManager());
        this.fogMap = fogMap;
        this.factionId = factionId;
    }

    @Override
    public List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();
        for (Faction f : getFactions()) {
            for (Unit u : f.getUnits()) {
                if (!u.isAlive()) continue;
                if (u.getFactionId().equals(factionId) || fogMap.isExplored(u.getQ(), u.getR())) {
                    units.add(u);
                }
            }
        }
        return units;
    }

    @Override
    public Unit findNearestHostile(Unit unit) {
        int myFactionId = Integer.parseInt(unit.getFactionId());
        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Unit u : getAllUnits()) {
            if (u == unit) continue;
            if (!u.isAlive()) continue;
            if (u.getFactionId().equals(unit.getFactionId())) continue;
            int otherId;
            try {
                otherId = Integer.parseInt(u.getFactionId());
            } catch (NumberFormatException e) { continue; }
            if (!getDiplomacyManager().isHostile(myFactionId, otherId)) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR());
            if (dist < minDist) {
                minDist = dist;
                nearest = u;
            }
        }
        return nearest;
    }

    @Override
    public Unit findNearestForeign(Unit unit) {
        Unit nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (Unit u : getAllUnits()) {
            if (u == unit) continue;
            if (!u.isAlive()) continue;
            if (u.getFactionId().equals(unit.getFactionId())) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR());
            if (dist < minDist) {
                minDist = dist;
                nearest = u;
            }
        }
        return nearest;
    }

    @Override
    public Unit findNearestEnemy(Unit unit) {
        return findNearestHostile(unit);
    }

    @Override
    public City findNearestEnemyCity(Unit unit) {
        int myFactionId = Integer.parseInt(unit.getFactionId());
        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : getFactions()) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            if (!getDiplomacyManager().isHostile(myFactionId, faction.getId())) continue;
            for (City city : faction.getCities()) {
                if (!city.isAlive()) continue;
                int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), city.getQ(), city.getR());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = city;
                }
            }
        }
        return nearest;
    }

    @Override
    public boolean isTileOccupied(int q, int r) {
        if (!fogMap.isExplored(q, r)) return false;
        return super.isTileOccupied(q, r);
    }

    @Override
    public boolean isTileOccupied(int q, int r, Unit self) {
        if (!fogMap.isExplored(q, r)) return false;
        return super.isTileOccupied(q, r, self);
    }
}
