package com.wrathborn.units;

import lombok.Data;

@Data
public class UnitStats {
    private String type;
    private int hp;
    private int attack;
    private int defense;
    private int speed;
    private int range;
    private int cost;
}