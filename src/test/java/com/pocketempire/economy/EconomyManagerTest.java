package com.pocketempire.economy;

import com.pocketempire.entities.Faction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EconomyManagerTest {

    private EconomyManager economy;

    @BeforeEach
    void setUp() {
        economy = new EconomyManager();
    }

    @Test
    void canAffordUnit_exactAmount() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(7);

        assertTrue(economy.canAffordUnit(faction, 7));
    }

    @Test
    void canAffordUnit_moreThanNeeded() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(20);

        assertTrue(economy.canAffordUnit(faction, 7));
    }

    @Test
    void canAffordUnit_notEnough() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(3);

        assertFalse(economy.canAffordUnit(faction, 7));
    }

    @Test
    void canAffordUnit_zeroGold() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(0);

        assertFalse(economy.canAffordUnit(faction, 5));
    }

    @Test
    void purchaseUnit_success() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(10);

        boolean result = economy.purchaseUnit(faction, 7);

        assertTrue(result);
        assertEquals(3, faction.getGold());
    }

    @Test
    void purchaseUnit_notEnoughGold() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(5);

        boolean result = economy.purchaseUnit(faction, 7);

        assertFalse(result);
        assertEquals(5, faction.getGold(), "Gold should not change on failed purchase");
    }

    @Test
    void purchaseUnit_exactAmount() {
        var faction = new Faction(1, "Red", 0xFF0000);
        faction.setGold(7);

        boolean result = economy.purchaseUnit(faction, 7);

        assertTrue(result);
        assertEquals(0, faction.getGold());
    }
}
