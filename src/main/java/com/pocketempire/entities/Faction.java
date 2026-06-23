package com.pocketempire.entities;

import com.pocketempire.config.FactionConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Faction {
    private int id;
    private String name;
    private int color;
    private ArrayList<Unit> units;
    private ArrayList<City> cities;
    @Setter private boolean isAlive;
    @Setter private boolean isAI;
    @Setter private int gold;
    @Setter private int goldEarnedSinceLastDividend;
    @Setter private int activeWarCount;
    @Setter private int victoryPoints;
    @Setter private Set<String> researchedTechs = new HashSet<>();
    @Setter private String currentResearch;
    @Setter private int researchProgress;
    @Setter private FactionConfig config;

    public Faction(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.units = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.isAlive = true;
        this.gold = 10;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public void addVictoryPoints(int points) {
        this.victoryPoints += points;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public void removeCity(City city) {
        cities.remove(city);
    }

    public int getUnitCount() {
        return units.size();
    }

    public int getCityCount() {
        return (int) cities.stream().filter(City::isAlive).count();
    }

    public boolean hasTech(String techId) {
        return researchedTechs != null && researchedTechs.contains(techId);
    }

    public void addResearchPoints(int points) {
        researchProgress += points;
    }
}
