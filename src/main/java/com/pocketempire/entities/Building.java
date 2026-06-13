package com.pocketempire.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Building {
    WALLS("Walls", 10, 0, 50, 0, 0, 0),
    MARKET("Market", 8, 5, 0, 0, 0, 0),
    FORGE("Forge", 8, 0, 0, 2, 0, 0),
    WORKSHOP("Workshop", 12, 0, 0, 0, 1, 0),
    BARRACKS("Barracks", 10, 0, 0, 0, 0, 1);

    private final String name;
    private final int productionCost;
    private final int goldBonus;
    private final int hpBonus;
    private final int productionBonus;
    private final int forgeBonus;
    private final int attackBonus;
}
