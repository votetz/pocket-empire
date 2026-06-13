package com.pocketempire.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Entity {
    protected final String id;
    @Setter protected int q;
    @Setter protected int r;
    @Setter protected int hp;
    @Setter protected int maxHp;

    public Entity(String id, int q, int r, int hp, int maxHp) {
        this.id = id;
        this.q = q;
        this.r = r;
        this.hp = hp;
        this.maxHp = maxHp;
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