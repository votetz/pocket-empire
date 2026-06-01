package com.pocketempire.units;

public enum UnitType {
    // combat units
    LIGHT(2),
    ARCHER(3),
    HEAVY(5),
    MAGE(5),
    SIEGE(5),

    // peaceful units
    SETTLER(1),
    WORKER(2);

    private final int cost;

    UnitType(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }
}
