package com.pocketempire.units;

import com.pocketempire.config.RoleConfigLoader;

import java.util.Map;
import java.util.Set;

public enum UnitRole {
    ASSAULT, TANK, ASSASSIN, SNIPER, SIEGE, SUPPORT, CIVILIAN, NAVAL_RAM, NAVAL_FIRE;

    private static Map<UnitRole, Set<UnitRole>> counters;

    private static Map<UnitRole, Set<UnitRole>> getCounters() {
        if (counters == null) {
            counters = RoleConfigLoader.getCounters();
        }
        return counters;
    }

    public boolean counters(UnitRole other) {
        return getCounters().getOrDefault(this, Set.of()).contains(other);
    }

    public int getAttackBonus(UnitRole target) {
        return counters(target) ? 2 : 0;
    }
}
