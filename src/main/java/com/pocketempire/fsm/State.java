package com.pocketempire.fsm;

import com.pocketempire.entities.Unit;
import com.pocketempire.world.World;

public interface State {
    void enter(Unit unit);
    void update(Unit unit, World world);
    void exit(Unit unit);
}