package com.pocketempire.tiles;

import lombok.Getter;

@Getter
public enum TileType {
    // terrestrial biomes
    GRASS   (1, false, '.'),
    PLAINS  (1, false, '"'),
    DESERT  (1, false, '~'),
    SAVANNA (1, false, ','),

    // types of forests
    FOREST  (2, false, 'f'),
    JUNGLE  (2, false, 'J'),
    TAIGA   (2, false, '▲'),
    TUNDRA  (2, false, '*'),

    // mountain
    MOUNTAIN(3, true,  '^'),
    CAVES   (2, false, '#'),

    // water
    WATER   (2, false,  '≈'),
    OCEAN   (3, true,  '='),
    SWAMPS  (2, false, '§');

    public final int movementCost; // speed factor
    public final boolean blocksMovement; // completely prevents walking
    public final char symbol; // symbol for console

    TileType(int movementCost, boolean blocksMovement, char symbol) {
        this.movementCost = movementCost;
        this.blocksMovement = blocksMovement;
        this.symbol = symbol;
    }
}