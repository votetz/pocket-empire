package com.wrathborn.entities;

public class City extends Entity {
    private String name;
    private int hp;
    private int maxHp;
    private int population;
    private int maxPopulation;
    private String factionId;
    private String leaderId;
    private int production;

    public City(String id, int x, int y, String name, int hp, int maxHp, int population, int maxPopulation, String factionId, String leaderId, int production) {
        super(id, x, y);
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

    public String getFactionId() {
        return factionId;
    }

    public String setFactionId() {
        return factionId;
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

    public String getLeaderId() {
        return leaderId;
    }

    public int getProduction() {
        return production;
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
}
