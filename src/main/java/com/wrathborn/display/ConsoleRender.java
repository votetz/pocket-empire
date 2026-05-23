package com.wrathborn.display;

import com.wrathborn.world.Map;

public class ConsoleRender {
    private final Map map;

    public ConsoleRender(Map map) {
        this.map = map;
    }

    public void render() {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                System.out.print(map.getTile(x, y).getType().symbol);
            }
            System.out.println();
        }
    }
}
