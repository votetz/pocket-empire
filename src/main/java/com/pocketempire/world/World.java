package com.pocketempire.world;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.Faction;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class World {
    private final Map map;
    private List<Faction> factions;

    public World(Map map, List<Faction> factions) {
        this.map = map;
        this.factions = factions;
    }

    public Map getMap() {
        return map;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();
        for (Faction faction : factions) {
            units.addAll(faction.getUnits());
        }
        return units;
    }
    public Unit findNearestEnemy(Unit unit) {
        return getAllUnits().stream()
                .filter(u -> !u.getFactionId().equals(unit.getFactionId()))
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(u -> HexUtils.getDistance(unit.getQ(), unit.getR(), u.getQ(), u.getR())))
                .orElse(null);
    }

    public City findNearestEnemyCity(Unit unit) {
        City nearest = null;
        int minDist = Integer.MAX_VALUE;
        for (var faction : factions) {
            if (String.valueOf(faction.getId()).equals(unit.getFactionId())) continue;
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
