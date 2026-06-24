package com.pocketempire.world;

import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.Faction;
import java.util.List;
import java.util.ArrayList;

public class World {
    private final Map map;
    private List<Faction> factions;
    private final DiplomacyManager diplomacyManager;
    private List<Unit> cachedAllUnits;

    public World(Map map, List<Faction> factions, DiplomacyManager diplomacyManager) {
        this.map = map;
        this.factions = factions;
        this.diplomacyManager = diplomacyManager;
    }

    public void invalidateCache() {
        this.cachedAllUnits = null;
    }

    public List<Unit> getAllUnits() {
        if (cachedAllUnits == null) {
            cachedAllUnits = new ArrayList<>();
            for (Faction faction : factions) {
                cachedAllUnits.addAll(faction.getUnits());
            }
        }
        return cachedAllUnits;
    }

    public DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    public Map getMap() { return map; }

    public List<Faction> getFactions() { return factions; }

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
            if (!diplomacyManager.isHostile(myFactionId, otherId)) continue;
            int dist = HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR());
            if (dist < minDist) {
                minDist = dist;
                nearest = u;
            }
        }
        return nearest;
    }

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

    public Unit findNearestEnemy(Unit unit) {
        return findNearestHostile(unit);
    }

    public City findNearestEnemyCity(Unit unit) {
        int myFactionId = Integer.parseInt(unit.getFactionId());
        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : factions) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
            if (!diplomacyManager.isHostile(myFactionId, faction.getId())) continue;
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

    public boolean isTileOccupied(int q, int r) {
        for (Unit u : getAllUnits()) {
            if (u.isAlive() && u.getQ() == q && u.getR() == r) return true;
        }
        return false;
    }

    public boolean isTileOccupied(int q, int r, Unit self) {
        for (Unit u : getAllUnits()) {
            if (u != self && u.isAlive() && u.getQ() == q && u.getR() == r) return true;
        }
        return false;
    }
}