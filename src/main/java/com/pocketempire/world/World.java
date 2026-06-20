package com.pocketempire.world;

import com.pocketempire.diplomacy.DiplomacyManager;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.Faction;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class World {
    private final Map map;
    private List<Faction> factions;
    private final DiplomacyManager diplomacyManager;

    public World(Map map, List<Faction> factions, DiplomacyManager diplomacyManager) {
        this.map = map;
        this.factions = factions;
        this.diplomacyManager = diplomacyManager;
    }

    public DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    public Map getMap() { return map; }

    public List<Faction> getFactions() { return factions; }

    public List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();
        for (Faction faction : factions) {
            units.addAll(faction.getUnits());
        }
        return units;
    }

    public Unit findNearestEnemy(Unit unit) {
        int myFactionId = Integer.parseInt(unit.getFactionId());
        return getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(u -> diplomacyManager.isHostile(myFactionId, Integer.parseInt(u.getFactionId())))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
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