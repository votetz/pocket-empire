package com.pocketempire.simulation;

import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;

public class CombatResolver {
    private static final GameEventBus bus = GameEventBus.getInstance();

    public static void resolveCombat(Unit attacker, Unit defender, int terrainBonus) {
        int damageToDefender = calculateDamage(attacker.getAttack(), defender.getDefense() + defender.getDefenseModifier() + terrainBonus);
        defender.takeDamage(damageToDefender);
        bus.publish(new GameEvent.UnitAttacked(attacker, defender, damageToDefender));

        int distance = calculateDistance(attacker, defender);
        if (defender.isAlive() && defender.getRange() >= distance) {
            int damageToAttacker = calculateDamage(defender.getAttack(), attacker.getDefense() + attacker.getDefenseModifier());
            attacker.takeDamage(damageToAttacker);
            bus.publish(new GameEvent.CounterAttacked(attacker, defender, damageToAttacker));
        }

        if (!defender.isAlive()) {
            bus.publish(new GameEvent.UnitDied(defender));
        }
        if (!attacker.isAlive()) {
            bus.publish(new GameEvent.UnitDied(attacker));
        }
    }

    public static void resolveCityAttack(Unit attacker, City city) {
        int damageToCity = calculateDamage(attacker.getAttack(), 0);
        city.takeDamage(damageToCity);
    }

    private static int calculateDamage(int attack, int defense) {
        int damage = attack - (defense / 2);
        return Math.max(1, damage);
    }

    private static int calculateDistance(Unit a, Unit b) {
        return HexUtils.getDistance(a.getQ(), a.getR(), b.getQ(), b.getR());
    }
}