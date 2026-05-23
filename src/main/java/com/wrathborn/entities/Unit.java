package com.wrathborn.entities;

public class Unit extends Entity{
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int speed;
    private int range;
    private int stamina;
    private int maxStamina;
    private String factionId;

    public Unit(String id, int x, int y, int hp, int maxHp, int attack, int defense, int speed, int range, int stamina, int maxStamina, String factionId) {
        super(id, x, y);
        this.hp = hp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.range = range;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.factionId = factionId;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRange() {
        return range;
    }

    public int getStamina() {
        return stamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public String getFactionId() {
        return factionId;
    }

    @Override
    public void update(){
        //todo update unit stats here
    }

    public boolean isAlive(){
        return hp > 0;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public void restoreStamina(int amount) {
        stamina = Math.min(maxStamina, stamina + amount);
    }

    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
}
