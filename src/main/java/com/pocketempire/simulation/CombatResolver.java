package com.pocketempire.simulation;

import com.pocketempire.config.StatusEffectConfig;
import com.pocketempire.config.StatusEffectConfigLoader;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.units.MageType;
import com.pocketempire.units.UnitType;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;

import java.util.Random;

public class CombatResolver {
    private static final GameEventBus bus = GameEventBus.getInstance();
    private static final Random rng = new Random();

    public static void resolveCombat(Unit attacker, Unit defender, int terrainBonus) {
        if (attacker.hasEffect("STUNNED")) return;

        StatusEffectConfig frozen = StatusEffectConfigLoader.getConfig("FROZEN");
        StatusEffectConfig burning = StatusEffectConfigLoader.getConfig("BURNING");
        StatusEffectConfig poisoned = StatusEffectConfigLoader.getConfig("POISONED");

        int attackMod = attacker.hasEffect(frozen) ? -1 : 0;
        int defenseMod = 0;
        if (defender.hasEffect(burning)) defenseMod -= 1;
        if (defender.hasEffect(frozen)) defenseMod -= 1;

        int ramBonus = 0;
        if (attacker.getUnitType() == UnitType.TRIREME) {
            int hexesMoved = attacker.getMovement() - attacker.getRemainingOD();
            ramBonus = Math.min(hexesMoved, 2);
        }

        int damageToDefender = calculateDamage(attacker.getAttack() + attackMod + ramBonus, defender.getDefense()
                + defender.getDefenseModifier() + defenseMod + terrainBonus);
        defender.takeDamage(damageToDefender);
        bus.publish(new GameEvent.UnitAttacked(attacker, defender, damageToDefender));

        if (ramBonus > 0 && attacker.isAlive()) {
            attacker.takeDamage(ramBonus);
            bus.publish(new GameEvent.TriremeRam(attacker, defender, ramBonus, ramBonus));
        }

        if (attacker.getUnitType() == UnitType.DROMON && defender.isAlive() && defender instanceof Unit u && rng.nextDouble() < 0.5) {
            u.applyEffect(burning, burning.getDefaultDuration());
            bus.publish(new GameEvent.StatusApplied(u, burning, burning.getDefaultDuration()));
        }

        if (attacker.getUnitType() == UnitType.MAGE && defender.isAlive() && defender instanceof Unit u2) {
            MageType mt = attacker.getMageType();
            if (mt != null && rng.nextDouble() < 0.3) {
                StatusEffectConfig effect = switch (mt) {
                    case FIRE -> burning;
                    case ICE -> frozen;
                    case POISON -> poisoned;
                    case TELEPORT -> null;
                };
                if (effect != null) {
                    u2.applyEffect(effect, effect.getDefaultDuration());
                    bus.publish(new GameEvent.StatusApplied(u2, effect, effect.getDefaultDuration()));
                }
            }
        }

        int distance = calculateDistance(attacker, defender);
        if (defender.isAlive() && defender.getRange() >= distance) {
            int counterAttackMod = defender.hasEffect(frozen) ? -1 : 0;
            int counterDefenseMod = attacker.hasEffect(burning) ? -1 : 0;

            boolean isGuardianCounter = defender.getUnitType() == UnitType.GUARDIAN;
            boolean isEntrenched = isGuardianCounter && defender.getUnitState() == com.pocketempire.fsm.UnitState.ENTRENCH;
            int guardianAttackBonus = isGuardianCounter ? (isEntrenched ? 2 : 1) : 0;
            double guardianStunChance = isGuardianCounter ? (isEntrenched ? 0.22 : 0.15) : 0;

            int damageToAttacker = calculateDamage(defender.getAttack() + counterAttackMod + guardianAttackBonus,
                    attacker.getDefense() + attacker.getDefenseModifier() + counterDefenseMod);
            attacker.takeDamage(damageToAttacker);
            bus.publish(new GameEvent.CounterAttacked(attacker, defender, damageToAttacker));

            if (isGuardianCounter && attacker.isAlive() && rng.nextDouble() < guardianStunChance) {
                StatusEffectConfig stunned = StatusEffectConfigLoader.getConfig("STUNNED");
                attacker.applyEffect(stunned, stunned.getDefaultDuration());
                bus.publish(new GameEvent.StatusApplied(attacker, stunned, stunned.getDefaultDuration()));
            }
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