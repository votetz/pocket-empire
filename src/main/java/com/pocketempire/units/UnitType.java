package com.pocketempire.units;

import lombok.Getter;

public enum UnitType {
    // combat units
    LIGHT(5),
    ARCHER(6),
    HEAVY(8),
    MAGE(8),
    SIEGE(8),

    // peaceful units
    SETTLER(4),
    WORKER(5);

    @Getter
    private final int cost;

    UnitType(int cost) {
        this.cost = cost;
    }
}
