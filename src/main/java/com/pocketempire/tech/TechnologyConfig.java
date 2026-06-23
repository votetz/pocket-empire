package com.pocketempire.tech;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyConfig {
    private String id;
    private String name;
    private String description;
    private int cost;
    private List<String> prerequisites;
    private List<String> unlocks;
    private TechCategory category;
    private Integer mageAtkBonus;
    private Integer mageEffectChanceBonus;
}
