package com.pocketempire.entities;

import lombok.Getter;

@Getter
public abstract class Entity {
    protected final String id;
    protected int q;
    protected int r;
    protected int hp;
    protected int maxHp;

    public Entity(String id, int q, int r, int hp, int maxHp) {
        this.id = id;
        this.q = q;
        this.r = r;
        this.hp = hp;
        this.maxHp = maxHp;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void move(int dq, int dr) {
        q += dq;
        r += dr;
    }

    public void update() {}

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    public void restoreHp(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
}