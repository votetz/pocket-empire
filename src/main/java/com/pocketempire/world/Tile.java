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
    @Setter private TileType type;
    @Setter private boolean improved;
    private boolean visible;
    private boolean explored;
    @Setter private int burningTurns = 0;
    @Setter private int recoverTurns = 0;

    public boolean isBurning() { return burningTurns > 0; }
    public boolean isScorched() { return getType() == TileType.SCORCHED_EARTH; }
}
