package com.pocketempire.entities;

import com.pocketempire.units.UnitType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class City extends Entity {
    private final String name;
    @Setter private int hp;
    @Setter private int maxHp;
    @Setter private int population;
    @Setter private int maxPopulation;
    @Setter private String factionId;
    @Setter private String leaderId;
    @Setter private int production;
    @Setter private int accumulatedProduction;
    @Setter private UnitType currentProductionType;
    @Setter private int borderRadius;

    public City(String id, int q, int r, String name, int hp, int maxHp, int population, int maxPopulation, String factionId, String leaderId, int production) {
        super(id, q, r);
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
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

    public boolean isAlive(){
        return hp > 0;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
}
