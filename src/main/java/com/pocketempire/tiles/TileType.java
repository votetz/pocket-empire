package com.pocketempire.tiles;

import com.pocketempire.config.TerrainConfigLoader;
import lombok.Getter;

@Getter
public enum TileType {
    GRASS   ('.', 1, false),
    PLAINS  ('"', 1, false),
    DESERT  ('~', 1, false),
    SAVANNA (',', 1, false),
    FOREST  ('f', 2, false),
    JUNGLE  ('J', 2, false),
    TAIGA   ('▲', 2, false),
    TUNDRA  ('*', 2, false),
    MOUNTAIN('^', 3, true),
    CAVES   ('#', 2, false),
    SHALLOWS('≈', 2, false),
    OCEAN   ('=', 3, true),
    SWAMPS  ('§', 2, false),
    SCORCHED_EARTH('&', 1, false);

    private final char symbol;
    private final int movementCost;
    private final boolean blocksMovement;
    private int defendBonus;
    private int attackModifier;

    TileType(char symbol, int movementCost, boolean blocksMovement) {
        this.symbol = symbol;
        this.movementCost = movementCost;
        this.blocksMovement = blocksMovement;
    }

    public void loadBonuses() {
        this.defendBonus = TerrainConfigLoader.getDefendBonus(this);
        this.attackModifier = TerrainConfigLoader.getAttackModifier(this);
    }

    public boolean isWater() {
        return this == SHALLOWS || this == OCEAN;
    }
}
