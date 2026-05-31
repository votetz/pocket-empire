package com.wrathborn.world;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.wrathborn.tiles.TileType;

@Getter
@AllArgsConstructor
public class Tile {
    private int q;  // axial coordinate q
    private int r;  // axial coordinate r
    private TileType type;

    // Cube coordinates for 3D rendering
    public int cubeX() {
        return q;
    }

    public int cubeY() {
        return -q - r;
    }

    public int cubeZ() {
        return r;
    }
}