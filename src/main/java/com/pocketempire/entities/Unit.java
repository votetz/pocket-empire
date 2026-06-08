package com.pocketempire.entities;

import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.fsm.IdleState;
import com.pocketempire.fsm.State;
import com.pocketempire.fsm.UnitState;
import com.pocketempire.units.UnitStats;
import com.pocketempire.units.UnitType;
import com.pocketempire.world.World;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Unit extends Entity {
    private final String name;
    private int attack;
    private int defense;
    private int speed;
    private final int range;
    private final int sightRange;
    private final int cost;
    @Setter private int defenseModifier;
    private final String factionId;
    private final UnitType unitType;
    @Setter private UnitState unitState;
    @Setter private State currentState;
    private int remainingOD;

    protected Unit(Builder builder) {
        super(builder.id, builder.q, builder.r, builder.hp, builder.maxHp);
        this.name = builder.name;
        this.attack = builder.attack;
        this.defense = builder.defense;
        this.speed = builder.speed;
        this.range = builder.range;
        this.sightRange = builder.sightRange;
        this.cost = builder.cost;
        this.factionId = builder.factionId;
        this.unitType = builder.unitType;
        this.unitState = builder.unitState;
        this.remainingOD = builder.speed;
        this.currentState = new IdleState();
    }

public static class Builder {
    private final String id;
    private final int q;
    private final int r;
    private final String factionId;
    private String name;

    private int hp = 10;
    private int maxHp = hp;
    private int attack = 2;
    private int defense = 1;
    private int speed = 1;
    private int range = 1;
    private int sightRange;
    private int cost = 1;
    private UnitType unitType;
    private UnitState unitState = UnitState.IDLE;

    public Builder(String id, int q, int r, String factionId) {
        this.id = id;
        this.q = q;
        this.r = r;
        this.factionId = factionId;
}

    public Builder name(String name) {
        this.name = name;
        return this;
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

    public Builder sightRange(int sightRange){
       this.sightRange = sightRange;
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

    public Builder unitType(UnitType unitType){
       this.unitType = unitType;
       return this;
    }

    public Builder config(UnitStats stats) {
        hp(stats.getHp());
        attack(stats.getAttack());
        defense(stats.getDefense());
        speed(stats.getSpeed());
        range(stats.getRange());
        sightRange(stats.getSpeed() + stats.getRange() + 1);
        cost(stats.getCost());
        return this;
    }

    public Unit build(){
       return new Unit(this);
    }
}

    public void resetOD() {
        this.remainingOD = speed;
    }

    public boolean spendOD(int cost) {
        if (remainingOD < cost) return false;
        remainingOD -= cost;
        return true;
    }


    public void updateAI(World world) {
        if (currentState != null) {
            currentState.update(this, world);
        }
    }

    public void changeState(State newState, UnitState stateEnum) {
        if (currentState != null) {
            currentState.exit(this);
        }
        this.currentState = newState;
        this.unitState = stateEnum;
        newState.enter(this);
        GameEventBus.getInstance().publish(new GameEvent.UnitStateChanged(this));
    }

}