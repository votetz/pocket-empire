package com.pocketempire.diplomacy;

import com.pocketempire.entities.Faction;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.World;

public class MilitaryDominance implements CasusBelli {
    private static final double DOMINANCE_MULTIPLIER = 2.0;

    @Override
    public String getId() { return "MILITARY_DOMINANCE"; }

    @Override
    public boolean check(Faction aggressor, Faction defender, World world, int currentTurn) {
        int myPower = calculatePower(aggressor);
        int enemyPower = calculatePower(defender);
        return myPower > enemyPower * DOMINANCE_MULTIPLIER;
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
