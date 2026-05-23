package com.wrathborn.tiles;

public enum TileType {
    // terrestrial biomes
    GRASS   (1.0, false, '.'),
    PLAINS  (1.2, false, '"'),
    DESERT  (0.7, false, '~'),
    SAVANNA (0.8, false, ','),

    // types of forests
    FOREST  (0.5, false, 'f'),
    JUNGLE  (0.3, false, 'J'),
    TAIGA   (0.4, false, '▲'),
    TUNDRA  (0.6, false, '*'),

    // mountain
    MOUNTAIN(0.1, true,  '^'),
    CAVES   (0.3, false, '#'),

    // water
    WATER   (0.1, true,  '≈'),
    OCEAN   (0.0, true,  '='),
    SWAMPS  (0.2, false, '§');

    public final double movementSpeed; // speed factor
    public final boolean blocksMovement; // completely prevents walking
    public final char symbol; // symbol for console

    TileType(double movementSpeed, boolean blocksMovement, char symbol) {
        this.movementSpeed = movementSpeed;
        this.blocksMovement = blocksMovement;
        this.symbol = symbol;
    }
}