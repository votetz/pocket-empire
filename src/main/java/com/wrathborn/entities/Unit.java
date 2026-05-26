package com.wrathborn.entities;

import com.wrathborn.fsm.State;
import com.wrathborn.fsm.UnitState;
import com.wrathborn.units.MovementType;
import com.wrathborn.world.World;

public class Unit extends Entity{
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int speed;
    private int range;;
    private String factionId;
    private UnitState unitState;
    private State currentState;


    public Unit(String id, int x, int y, int hp, int maxHp, int attack, int defense, int speed, int range, String factionId, UnitState unitState) {
        super(id, x, y);
        this.hp = hp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.range = range;
        this.factionId = factionId;
        this.unitState = unitState;
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

    public String getFactionId() {
        return factionId;
    }

    public UnitState getUnitState() {
        return unitState;
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
