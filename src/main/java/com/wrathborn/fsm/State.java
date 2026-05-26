package com.wrathborn.fsm;

import com.wrathborn.entities.Unit;
import java.util.List;

public interface State {
    void enter(Unit unit);
    void update(Unit unit, List<Unit> allUnits);
    void exit(Unit unit);
}