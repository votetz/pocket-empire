package com.pocketempire.world;

import lombok.*;
import com.pocketempire.tiles.TileType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Tile {
    private int q;
    private int r;
    private TileType type;
    @Setter private boolean improved;
    private boolean visible;
    private boolean explored;

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