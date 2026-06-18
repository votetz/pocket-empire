package com.pocketempire.tech;

import lombok.Getter;

public enum TechCategory {
    MILITARY(0),
    ECONOMIC(1),
    NAVAL(2);

    @Getter
    private final int priority;

    TechCategory(int priority) {
        this.priority = priority;
    }
}
