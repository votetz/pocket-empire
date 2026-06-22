package com.pocketempire.diplomacy;

import com.pocketempire.entities.Faction;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.World;

public class WeakNeighbor implements CasusBelli {
    private static final double WEAKNESS_MULTIPLIER = 1.5;

    @Override
    public String getId() { return "WEAK_NEIGHBOR"; }

    @Override
    public boolean check(Faction aggressor, Faction defender, World world, int currentTurn) {
        int myPower = calculatePower(aggressor);
        int enemyPower = calculatePower(defender);

        if (myPower < enemyPower * WEAKNESS_MULTIPLIER) return false;

        return defender.getCityCount() < aggressor.getCityCount();
    }

    private int calculatePower(Faction faction) {
        int power = 0;
        for (var unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            if (unit.getUnitType() == UnitType.SETTLER || unit.getUnitType() == UnitType.WORKER) continue;
            power += unit.getAttack() + unit.getDefense() + unit.getHp();
        }
        return power;
    }
}
