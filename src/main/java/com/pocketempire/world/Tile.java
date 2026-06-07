package com.pocketempire.world;

import lombok.*;
import com.pocketempire.tiles.TileType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Tile {
    private int q;
    private int r;
    private TileType type;
    private boolean improved;

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