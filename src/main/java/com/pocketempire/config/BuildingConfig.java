package com.pocketempire.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingConfig {
    private String name;
    private int productionCost;
    private int goldBonus;
    private int hpBonus;
    private int productionBonus;
    private int forgeBonus;
    private int attackBonus;
    private int improvedTileGoldBonus;
}
