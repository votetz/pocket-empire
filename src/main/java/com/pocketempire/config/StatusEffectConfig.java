package com.pocketempire.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusEffectConfig {
    private String name;
    private int tickDamage;
    private int defaultDuration;
    private String icon;
}
