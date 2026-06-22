package com.pocketempire.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactionConfig {
    private String id;
    private String name;
    private String theme;
    private String icon;
    private String color;
    private String description;

    /*
    private int militaryState;
    private int economyState;
    private int scienceStat;
    */

    private int atkBonus;
    private int movementBonus;
    private int entrenchBonus;
    private double effectChanceBonus;
    private int forestDefBonus;

    private int lightUnitCostReduction;
    private int catapultCostReduction;
    private double researchMultiplier;
    private int wallCostReductionPercent;

    private int startingReputation;
    private int startingGold;
    private int borderContactRepDrain;
    private int betrayalThreshold;

    private List<String> preferredUnits;
    private String aiPersonality;
}
