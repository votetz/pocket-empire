package com.wrathborn.simulation;

import com.wrathborn.entities.Unit;
import com.wrathborn.entities.City;

public class CombatResolver {
    public static void resolveCombat(Unit attacker, Unit defender) {
        int damageToDefender = calculateDamage(attacker.getAttack(), defender.getDefense());
        defender.takeDamage(damageToDefender);

        if (defender.isAlive() && defender.getRange() >= calculateDistance(attacker, defender)) {
            int damageToAttacker = calculateDamage(defender.getAttack(), attacker.getDefense());
            attacker.takeDamage(damageToAttacker);
        }
    }

    public static void resolveCityAttack(Unit attacker, City city) {
        int damageToCity = calculateDamage(attacker.getAttack(), 0); // Cities might have base defense logic later
        city.takeDamage(damageToCity);
    }

    private static int calculateDamage(int attack, int defense) {
        int damage = attack - (defense / 2);
        return Math.max(1, damage);
    }

    private static int calculateDistance(Unit a, Unit b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

}
