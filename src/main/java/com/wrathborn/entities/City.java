package com.wrathborn.entities;

public class City extends Entity {
    private final String name;
    private int hp;
    private int maxHp;
    private int population;
    private int maxPopulation;
    private String factionId;
    private String leaderId;
    private int production;

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
    }

    public String getName() {
        return name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public void setMaxPopulation(int maxPopulation) {
        this.maxPopulation = maxPopulation;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getProduction() {
        return production;
    }

    public void setProduction(int production) {
        this.production = production;
    }

    @Override
    public void update() {
        //todo update city stats here
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
