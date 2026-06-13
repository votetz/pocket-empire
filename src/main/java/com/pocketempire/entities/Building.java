package com.pocketempire.entities;

import lombok.Getter;

@Getter
public enum Building {
    WALLS("Walls", 10, 0, 50, 0),
    MARKET("Market", 8, 5, 0, 0),
    FORGE("Forge", 8, 0, 0, 2);
    // WORKSHOP("Workshop", 0, 0, 0, 1);

    private final String name;
    private final int productionCost;
    private final int goldBonus;
    private final int hpBonus;
    private final int productionBonus;


    Building(String name, int productionCost, int goldBonus, int hpBonus, int productionBonus) {
        this.name = name;
        this.productionCost = productionCost;
        this.goldBonus = goldBonus;
        this.hpBonus = hpBonus;
        this.productionBonus = productionBonus;
    }
}
