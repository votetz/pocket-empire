package com.pocketempire.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusEffect {
    BURNING(2,3),
    FROZEN(0, 2),
    POISONED(1,3),
    STUNNED(0,1);

    private final int tickDamage;
    private final int defaultDuration;
}
