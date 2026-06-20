package com.pocketempire.diplomacy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DiplomaticStatus {
    WAR(-100, -30),
    NEUTRAL(-29, 29),
    ALLIED(30, 69),
    DEVOTED(70, 100);

    private final int minValue;
    private final int maxValue;

    public static DiplomaticStatus fromValue(int value) {
        for (DiplomaticStatus s : values()) {
            if (value >= s.minValue && value <= s.maxValue ) return s;
        }
        return NEUTRAL;
    }
}
