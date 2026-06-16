package com.pocketempire.units;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnitFactoryTest {

    @Test
    void create_lightUnit_hasCorrectStats() {
        var unit = UnitFactory.create(UnitType.LIGHT, "u1", "Soldier", 0, 0, "f1");

        assertNotNull(unit);
        assertEquals(UnitType.LIGHT, unit.getUnitType());
        assertEquals("Soldier", unit.getName());
        assertEquals("f1", unit.getFactionId());
        assertEquals(0, unit.getQ());
        assertEquals(0, unit.getR());
        // LIGHT: hp=12, attack=3, defense=2, movement=2, range=1
        assertEquals(12, unit.getMaxHp());
        assertEquals(3, unit.getAttack());
        assertEquals(2, unit.getDefense());
        assertEquals(2, unit.getMovement());
        assertEquals(1, unit.getRange());
    }

    @Test
    void create_archerUnit_hasRange2() {
        var unit = UnitFactory.create(UnitType.ARCHER, "u2", "Archer", 1, 1, "f1");

        assertEquals(UnitType.ARCHER, unit.getUnitType());
        // ARCHER: range=2, hp=10, attack=3
        assertEquals(2, unit.getRange());
        assertEquals(10, unit.getMaxHp());
        assertEquals(3, unit.getAttack());
    }

    @Test
    void create_settlerUnit_hasZeroAttack() {
        var unit = UnitFactory.create(UnitType.SETTLER, "u4", "Settler", 5, 5, "f2");

        assertEquals(UnitType.SETTLER, unit.getUnitType());
        // SETTLER: attack=0, hp=5
        assertEquals(0, unit.getAttack());
        assertEquals(5, unit.getMaxHp());
    }

    @Test
    void create_scoutUnit_hasHighMovement() {
        var unit = UnitFactory.create(UnitType.SCOUT, "u5", "Scout", 0, 0, "f1");

        // SCOUT: movement=4, attack=1
        assertEquals(4, unit.getMovement());
        assertEquals(1, unit.getAttack());
    }

    @Test
    void create_setsPosition() {
        var unit = UnitFactory.create(UnitType.HEAVY, "u6", "Knight", 10, 20, "f3");

        assertEquals(10, unit.getQ());
        assertEquals(20, unit.getR());
    }
}
