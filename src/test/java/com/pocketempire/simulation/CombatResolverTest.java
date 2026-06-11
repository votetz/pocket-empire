package com.pocketempire.simulation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CombatResolverTest {

    @Test
    void calculateDamage_formula() {
        // attack - (defense / 2), min 1
        // 10 - (6 / 2) = 10 - 3 = 7
        assertEquals(7, CombatResolver.calculateDamage(10, 6));
    }

    @Test
    void calculateDamage_zeroDefense() {
        // 8 - (0 / 2) = 8
        assertEquals(8, CombatResolver.calculateDamage(8, 0));
    }

    @Test
    void calculateDamage_defenseExceedsAttack() {
        // 3 - (10 / 2) = 3 - 5 = -2 -> min 1
        assertEquals(1, CombatResolver.calculateDamage(3, 10));
    }

    @Test
    void calculateDamage_equalValues() {
        // 5 - (5 / 2) = 5 - 2 = 3
        assertEquals(3, CombatResolver.calculateDamage(5, 5));
    }

    @Test
    void calculateDamage_highDefense() {
        // 1 - (100 / 2) = 1 - 50 = -49 → min 1
        assertEquals(1, CombatResolver.calculateDamage(1, 100));
    }

    @Test
    void calculateDamage_lowDefense() {
        // 1 - (1 / 2) = 1 - 0 = 1 (integer division)
        assertEquals(1, CombatResolver.calculateDamage(1, 1));
    }
}
