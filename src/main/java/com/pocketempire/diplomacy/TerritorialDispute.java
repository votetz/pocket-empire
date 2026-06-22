package com.pocketempire.diplomacy;

import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

public class TerritorialDispute implements CasusBelli {
    private static final int MIN_CITY_DISTANCE = 8;

    @Override
    public String getId() { return "TERRITORIAL_DISPUTE"; }

    @Override
    public boolean check(Faction aggressor, Faction defender, World world, int currentTurn) {
        for (City ca : aggressor.getCities()) {
            if (!ca.isAlive()) continue;
            for (City cb : defender.getCities()) {
                if (!cb.isAlive()) continue;
                int dist = HexUtils.getDistance(ca.getQ(), ca.getR(), cb.getQ(), cb.getR());
                if (dist < MIN_CITY_DISTANCE) return true;
            }
        }
        return false;
    }
}
