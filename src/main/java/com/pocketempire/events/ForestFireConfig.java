package com.pocketempire.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForestFireConfig {
    private double chance;
    private double spreadChance;
    private int burnDuration;
    private int recoverDuration;
    private int damagePerTurn;
}
