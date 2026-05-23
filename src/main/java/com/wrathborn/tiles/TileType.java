package com.wrathborn.tiles;

public enum TileType {
    GRASS   (1.0, false, '.'),
    PLAINS  (1.2, false, '_'),
    DESERT  (0.7, false, 'd'),
    SAVANNA (0.8, false, 's'),
    FOREST  (0.5, false, 'T'),
    JUNGLE  (0.3, false, 'J'),
    TAIGA   (0.4, false, 't'),
    TUNDRA  (0.6, false, 'u'),
    MOUNTAIN(0.2, true,  '^'),
    CAVES   (0.3, false, 'c'),
    WATER   (0.0, true,  '~'),
    OCEAN   (0.0, true,  'O');

    public final double movementSpeed; // speed factor
    public final boolean blocksMovement; // completely prevents walking
    public final char symbol; // symbol for console

    TileType(double movementSpeed, boolean blocksMovement, char symbol) {
        this.movementSpeed = movementSpeed;
        this.blocksMovement = blocksMovement;
        this.symbol = symbol;
    }
}