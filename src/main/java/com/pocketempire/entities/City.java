package com.pocketempire.entities;

import com.pocketempire.units.UnitType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class City extends Entity {
    private final String name;
    private final int population;
    private final int maxPopulation;
    private final String factionId;
    private final String leaderId;
    private final int production;
    @Setter private int accumulatedProduction;
    @Setter private UnitType currentProductionType;
    private final int borderRadius;

    public City(String id, int q, int r, String name, int hp, int maxHp, int population, int maxPopulation, String factionId, String leaderId, int production) {
        super(id, q, r, hp, maxHp);
        this.name = name;
        this.population = population;
        this.maxPopulation = maxPopulation;
        this.factionId = factionId;
        this.leaderId = leaderId;
        this.production = production;
        this.accumulatedProduction = 0;
        this.currentProductionType = null;
        this.borderRadius = 3;
    }

    @Override
    public void update() {
        if (isAlive() && currentProductionType != null) {
            accumulatedProduction += production;
        }
    }

}
