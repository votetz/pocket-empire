package com.pocketempire.units;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitStats {
    private String type;
    private int hp;
    private int attack;
    private int defense;
    private int movement;
    private int range;
    private int cost;
    private MovementType movementType;
    @com.fasterxml.jackson.annotation.JsonProperty("role") private UnitRole unitRole;
    private double effectChance;
    private String requiredTech;
    private Map<String, UnitRole> roleByAbility;
}
