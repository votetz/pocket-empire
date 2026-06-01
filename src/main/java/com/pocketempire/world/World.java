package com.pocketempire.world;

import com.pocketempire.entities.Unit;
import com.pocketempire.entities.Faction;
import java.util.List;
import java.util.ArrayList;

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
    void update() {}
}
