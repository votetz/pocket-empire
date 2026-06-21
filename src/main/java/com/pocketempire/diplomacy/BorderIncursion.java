package com.pocketempire.diplomacy;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

public class BorderIncursion implements CasusBelli {
    private static final int INCURSION_RADIUS = 5;

    @Override
    public String getId() { return "BORDER_INCURSION"; }

    @Override
    public boolean check(Faction aggressor, Faction defender, World world) {
        for (City city : aggressor.getCities()) {
            if (!city.isAlive()) continue;
            for (var unit : world.getAllUnits()) {
                if (!unit.isAlive()) continue;
                if (!unit.getFactionId().equals(String.valueOf(defender.getId()))) continue;
                int dist = HexUtils.getDistance(city.getQ(), city.getR(), unit.getQ(), unit.getR());
                if (dist <= INCURSION_RADIUS) return true;
            }
        }
        return false;
    }
}
