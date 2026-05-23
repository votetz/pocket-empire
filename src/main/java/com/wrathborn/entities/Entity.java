package com.wrathborn.entities;

public abstract class Entity {
    protected String id;
    protected int x;
    protected int y;

    public Entity(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public abstract void update();

}