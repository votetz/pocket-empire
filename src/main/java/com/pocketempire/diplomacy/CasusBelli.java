package com.pocketempire.diplomacy;

import com.pocketempire.entities.Faction;
import com.pocketempire.world.World;

public interface CasusBelli {
    String getId();
    boolean check(Faction aggressor, Faction defender, World world, int currentTurn);

}
