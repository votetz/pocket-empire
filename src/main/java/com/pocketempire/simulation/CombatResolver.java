package com.pocketempire.simulation;

import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.world.HexUtils;

public class CombatResolver {
    public static void resolveCombat(Unit attacker, Unit defender) {
        int damageToDefender = calculateDamage(attacker.getAttack(), defender.getDefense());
        defender.takeDamage(damageToDefender);
        System.out.println(attacker.getName() + " deals " + damageToDefender + " damage to " + defender.getName()
                + " (" + defender.getHp() + "/" + defender.getMaxHp() + " HP)");

        // CA only when enemy is in range
        int distance = calculateDistance(attacker, defender);
        if (defender.isAlive() && defender.getRange() >= distance) {
            int damageToAttacker = calculateDamage(defender.getAttack(), attacker.getDefense());
            attacker.takeDamage(damageToAttacker);
            System.out.println(defender.getName() + " counter-attacks for " + damageToAttacker + " damage to "
                    + attacker.getName() + " (" + attacker.getHp() + "/" + attacker.getMaxHp() + " HP)");
        }

        if (!defender.isAlive()) {
            System.out.println(defender.getName() + " has been destroyed!");
        }
        if (!attacker.isAlive()) {
            System.out.println(attacker.getName() + " has been destroyed!");
        }
    }

    public static void resolveCityAttack(Unit attacker, City city) {
        int damageToCity = calculateDamage(attacker.getAttack(), 0);
        // Cities might have base defense logic later
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