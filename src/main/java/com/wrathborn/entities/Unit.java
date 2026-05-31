package com.wrathborn.entities;

import com.wrathborn.fsm.IdleState;
import com.wrathborn.fsm.State;
import com.wrathborn.fsm.UnitState;import com.wrathborn.units.UnitStats;
import com.wrathborn.world.World;

public class Unit extends Entity {
    private int hp;
    private final int maxHp;
    private int attack;
    private int defense;
    private int speed;
    private final int range;
    private final int cost;
    private final String factionId;
    private UnitState unitState;
    private State currentState;

    protected Unit(Builder builder) {
        super(builder.id, builder.q, builder.r);
        this.hp = builder.hp;
        this.maxHp = builder.maxHp;
        this.attack = builder.attack;
        this.defense = builder.defense;
        this.speed = builder.speed;
        this.range = builder.range;
        this.cost = builder.cost;
        this.factionId = builder.factionId;
        this.unitState = builder.unitState;
        this.currentState = new IdleState();
    }

public static class Builder {
    private final String id;
    private final int q;
    private final int r;
    private final String factionId;

    private int hp = 10;
    private int maxHp = hp;
    private int attack = 2;
    private int defense = 1;
    private int speed = 1;
    private int range = 1;
    private int cost = 1;
    private UnitState unitState = UnitState.IDLE;

    public Builder(String id, int q, int r, String factionId) {
        this.id = id;
        this.q = q;
        this.r = r;
        this.factionId = factionId;
}

    public Builder hp(int hp) {
        this.hp = hp;
        this.maxHp = hp;
        return this;
    }

    public Builder attack(int attack){
       this.attack = attack;
       return this;
    }

    public Builder defense(int defense){
       this.defense = defense;
       return this;
    }

    public Builder speed(int speed){
       this.speed = speed;
       return this;
    }

    public Builder range(int range){
       this.range = range;
       return this;
    }

    public Builder cost(int cost){
       this.cost = cost;
       return this;
    }

    public Builder unitState(UnitState unitState){
       this.unitState = unitState;
       return this;
    }

    public Builder config(UnitStats stats) {
        hp(stats.hp);
        attack(stats.attack);
        defense(stats.defense);
        speed(stats.speed);
        range(stats.range);
        cost(stats.cost);
        return this;
    }

    public Unit build(){
       return new Unit(this);
    }
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

    public int getCost() {
        return cost;
    }

    public String getFactionId() {
        return factionId;
    }

    public UnitState getUnitState() {
        return unitState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setUnitState(UnitState unitState) {
        this.unitState = unitState;
    }

    @Override
    public void update(){}

    public void updateAI(World world) {
        if (currentState != null) {
            currentState.update(this, world.getAllUnits());
        }
    }

    public void changeState(State newState, UnitState stateEnum) {
        if (currentState != null) {
            currentState.exit(this);
        }
        this.currentState = newState;
        this.unitState = stateEnum;
        newState.enter(this);
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