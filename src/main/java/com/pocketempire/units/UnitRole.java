package com.pocketempire.units;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum UnitRole {
    ASSAULT, TANK, ASSASSIN, SNIPER, SIEGE, SUPPORT, CIVILIAN, NAVAL_RAM, NAVAL_FIRE;

    private static final Map<UnitRole, Set<UnitRole>> COUNTERS = new HashMap<>();

    static {
        COUNTERS.put(ASSASSIN, Set.of(SNIPER, SIEGE, SUPPORT));
        COUNTERS.put(SNIPER,   Set.of(TANK));
        COUNTERS.put(TANK,     Set.of(ASSAULT));
        COUNTERS.put(ASSAULT,  Set.of(ASSASSIN));
        COUNTERS.put(NAVAL_RAM, Set.of(NAVAL_FIRE));
    }

    public boolean counters(UnitRole other) {
        return COUNTERS.getOrDefault(this, Set.of()).contains(other);
    }

    public double getMultiplier(UnitRole target) {
        return counters(target) ? 1.3 : 1.0;
    }
}

