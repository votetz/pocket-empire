package com.pocketempire.diplomacy;

import com.pocketempire.entities.Faction;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.World;

public class WeakNeighbor implements CasusBelli {
    private static final int RECENT_LOSSES_THRESHOLD = 2;
    private static final int RECENT_LOSSES_WINDOW = 5;

    @Override
    public String getId() { return "WEAK_NEIGHBOR"; }

    @Override
    public boolean check(Faction aggressor, Faction defender, World world) {
        long combatUnits = defender.getUnits().stream()
                .filter(u -> u.isAlive() && u.getUnitType() != UnitType.SETTLER && u.getUnitType() != UnitType.WORKER)
                .count();
        return combatUnits <= RECENT_LOSSES_THRESHOLD;
    }
}
