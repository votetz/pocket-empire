package com.pocketempire.simulation;

import com.pocketempire.entities.StatusEffect;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.units.UnitType;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;

import java.util.Random;

public class CombatResolver {
    private static final GameEventBus bus = GameEventBus.getInstance();
    private static final Random rng = new Random();

    public static void resolveCombat(Unit attacker, Unit defender, int terrainBonus) {
        if (attacker.hasEffect(StatusEffect.STUNNED)) return;

        int attackMod = attacker.hasEffect(StatusEffect.FROZEN) ? -1 : 0;
        int defenseMod = 0;
        if (defender.hasEffect(StatusEffect.BURNING)) defenseMod -= 1;
        if (defender.hasEffect(StatusEffect.FROZEN)) defenseMod -= 1;

        int damageToDefender = calculateDamage(attacker.getAttack() + attackMod, defender.getDefense()
                + defender.getDefenseModifier() + defenseMod + terrainBonus);
        defender.takeDamage(damageToDefender);
        bus.publish(new GameEvent.UnitAttacked(attacker, defender, damageToDefender));

        if (attacker.getUnitType() == UnitType.DROMON && defender.isAlive() && defender instanceof Unit u) {
            u.applyEffect(StatusEffect.BURNING, StatusEffect.BURNING.getDefaultDuration());
            bus.publish(new GameEvent.StatusApplied(u, StatusEffect.BURNING, StatusEffect.BURNING.getDefaultDuration()));
        }

        if (attacker.getUnitType() == UnitType.MAGE && defender.isAlive() && defender instanceof Unit u2 && rng.nextDouble() < 0.3) {
            u2.applyEffect(StatusEffect.BURNING, StatusEffect.BURNING.getDefaultDuration());
            bus.publish(new GameEvent.StatusApplied(u2, StatusEffect.BURNING, StatusEffect.BURNING.getDefaultDuration()));
        }

        int distance = calculateDistance(attacker, defender);
        if (defender.isAlive() && defender.getRange() >= distance) {
            int counterAttackMod = defender.hasEffect(StatusEffect.FROZEN) ? -1 : 0;
            int counterDefenseMod = attacker.hasEffect(StatusEffect.BURNING) ? -1 : 0;
            int damageToAttacker = calculateDamage(defender.getAttack() + counterAttackMod,
                    attacker.getDefense() + attacker.getDefenseModifier() + counterDefenseMod);
            attacker.takeDamage(damageToAttacker);
            bus.publish(new GameEvent.CounterAttacked(attacker, defender, damageToAttacker));
        }

        if (!defender.isAlive()) {
            bus.publish(new GameEvent.UnitDied(defender, attacker));
        }
        if (!attacker.isAlive()) {
            bus.publish(new GameEvent.UnitDied(attacker, defender));
        }
    }

    public static void resolveCityAttack(Unit attacker, City city) {
        int damageToCity = calculateDamage(attacker.getAttack(), 0);
        city.takeDamage(damageToCity);
        bus.publish(new GameEvent.CityAttacked(attacker, city, damageToCity));
    }

    static int calculateDamage(int attack, int defense) {
        int damage = attack - (defense / 2);
        return Math.max(1, damage);
    }

    private static int calculateDistance(Unit a, Unit b) {
        return HexUtils.getDistance(a.getQ(), a.getR(), b.getQ(), b.getR());
    }
}