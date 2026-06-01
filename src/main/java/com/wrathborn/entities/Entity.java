package com.wrathborn.entities;

import lombok.Getter;

@Getter
public abstract class Entity {
    protected final String id;
    protected int q;
    protected int r;

    public Entity(String id, int q, int r) {
        this.id = id;
        this.q = q;
        this.r = r;
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

    public abstract void update();

}