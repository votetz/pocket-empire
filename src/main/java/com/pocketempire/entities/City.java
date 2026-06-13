package com.pocketempire.entities;

import com.pocketempire.config.BuildingConfig;
import com.pocketempire.units.UnitType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private final List<BuildingConfig> buildings;

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
        this.buildings = new ArrayList<>();
    }

    public void addBuilding(BuildingConfig building) {
        buildings.add(building);
        if (building.getHpBonus() > 0) {
            setMaxHp(getMaxHp() + building.getHpBonus());
            setHp(getHp() + building.getHpBonus());
        }
    }

    public boolean hasBuilding(BuildingConfig building) {
        return buildings.contains(building);
    }

    public boolean hasBuilding(String name) {
        return buildings.stream().anyMatch(b -> b.getName().equals(name));
    }

    public int getGoldBonus() {
        return buildings.stream().mapToInt(BuildingConfig::getGoldBonus).sum();
    }

    public int getProductionBonus() {
        int forgeCount = (int) buildings.stream()
                .filter(b -> b.getName().equals("Forge"))
                .count();
        int forgeBonusTotal = forgeCount * buildings.stream()
                .filter(b -> b.getName().equals("Workshop"))
                .mapToInt(BuildingConfig::getForgeBonus)
                .sum();
        return buildings.stream().mapToInt(BuildingConfig::getProductionBonus).sum() + forgeBonusTotal;
    }

    public int getAttackBonus() {
        return buildings.stream().mapToInt(BuildingConfig::getAttackBonus).sum();
    }

    public int getImprovedTileGoldBonus() {
        return buildings.stream().mapToInt(BuildingConfig::getImprovedTileGoldBonus).sum();
    }

    public int getEffectiveProduction() {
        return production + getProductionBonus();
    }

    @Override
    public void update() {
        if (isAlive()) {
            accumulatedProduction += getEffectiveProduction();
        }
    }
}
