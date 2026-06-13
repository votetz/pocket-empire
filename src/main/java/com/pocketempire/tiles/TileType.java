package com.pocketempire.tiles;

import lombok.Getter;

@Getter
public enum TileType {
    // terrestrial biomes
    GRASS   (1, false, '.', 0),
    PLAINS  (1, false, '"', 0),
    DESERT  (1, false, '~', 0),
    SAVANNA (1, false, ',', 0),

    // types of forests
    FOREST  (2, false, 'f', 1),
    JUNGLE  (2, false, 'J', 1),
    TAIGA   (2, false, '▲', 1),
    TUNDRA  (2, false, '*', 1),

    // mountain
    MOUNTAIN(3, true,  '^', 0),
    CAVES   (2, false, '#', 2),

    // water
    WATER   (2, false,  '≈', 0),
    OCEAN   (3, true,  '=', 0),
    SWAMPS  (2, false, '§', 0);

    public final int movementCost; // speed factor
    public final boolean blocksMovement; // completely prevents walking
    public final char symbol; // symbol for console
    public final int defendBonus;
    public boolean isWater() {
        return this == WATER || this == OCEAN;
    }

    TileType(int movementCost, boolean blocksMovement, char symbol, int defendBonus) {
        this.movementCost = movementCost;
        this.blocksMovement = blocksMovement;
        this.symbol = symbol;
        this.defendBonus = defendBonus;
    }
}