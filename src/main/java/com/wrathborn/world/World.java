package com.wrathborn.world;

public class World {
    private final Map map;

    public World(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    void update() {}
}
