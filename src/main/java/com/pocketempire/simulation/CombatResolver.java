package com.pocketempire.simulation;

import com.pocketempire.config.StatusEffectConfig;
import com.pocketempire.config.StatusEffectConfigLoader;
import com.pocketempire.entities.Unit;
import com.pocketempire.entities.City;
import com.pocketempire.units.AbilityType;
import com.pocketempire.units.UnitRole;
import com.pocketempire.units.UnitType;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.World;

import java.util.Random;

public class CombatResolver {
    private static final GameEventBus bus = GameEventBus.getInstance();
    private static final Random rng = new Random();

    public static void resolveCombat(Unit attacker, Unit defender, int terrainBonus, int attackerTerrainModifier) {
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

        int damageToDefender = calculateDamage(attacker.getAttack() + attackMod + ramBonus + attackerTerrainModifier, defender.getDefense()
                + defender.getDefenseModifier() + defenseMod + terrainBonus,
                attacker.getUnitRole(), defender.getUnitRole());
        defender.takeDamage(damageToDefender);
        bus.publish(new GameEvent.UnitAttacked(attacker, defender, damageToDefender));

        if (ramBonus > 0 && attacker.isAlive()) {
            attacker.takeDamage(ramBonus);
            bus.publish(new GameEvent.TriremeRam(attacker, defender, ramBonus, ramBonus));
        }

        if (attacker.getUnitType() == UnitType.DROMON && defender.isAlive() && defender instanceof Unit u && rng.nextDouble() < attacker.getEffectChance()) {
            u.applyEffect(burning, burning.getDefaultDuration());
            bus.publish(new GameEvent.StatusApplied(u, burning, burning.getDefaultDuration()));
        }

        if (attacker.getUnitType() == UnitType.MAGE && defender.isAlive() && defender instanceof Unit u2) {
            AbilityType mt = attacker.getAbilityType();
            if (mt != null && rng.nextDouble() < attacker.getEffectChance()) {
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
                    attacker.getDefense() + attacker.getDefenseModifier() + counterDefenseMod,
                    defender.getUnitRole(), attacker.getUnitRole());
            attacker.takeDamage(damageToAttacker);
            bus.publish(new GameEvent.CounterAttacked(attacker, defender, damageToAttacker));

            if (isGuardianCounter && attacker.isAlive() && rng.nextDouble() < guardianStunChance) {
                StatusEffectConfig stunned = StatusEffectConfigLoader.getConfig("STUNNED");
                attacker.applyEffect(stunned, stunned.getDefaultDuration());
                bus.publish(new GameEvent.StatusApplied(attacker, stunned, stunned.getDefaultDuration()));
            }
        }

        if (!defender.isAlive()) {
            attacker.addXp(defender.getCost() / 2);
            bus.publish(new GameEvent.UnitDied(defender, attacker));
        }
        if (!attacker.isAlive()) {
            defender.addXp(attacker.getCost() / 2);
            bus.publish(new GameEvent.UnitDied(attacker, defender));
        }
    }

    public static void resolveCityAttack(Unit attacker, City city, World world) {
        int damageToCity = calculateDamage(attacker.getAttack(), 0, attacker.getUnitRole(), null);

        if (city.getHp() <= damageToCity) {
            if (shouldCapture(attacker, world)) {
                String oldFactionId = city.getFactionId();
                city.capture(attacker.getFactionId());

                for (var faction : world.getFactions()) {
                    if (String.valueOf(faction.getId()).equals(oldFactionId)) {
                        faction.removeCity(city);
                    }
                    if (String.valueOf(faction.getId()).equals(attacker.getFactionId())) {
                        faction.addCity(city);
                    }
                }

                bus.publish(new GameEvent.CityCaptured(city, attacker, oldFactionId));
            } else {
                city.takeDamage(damageToCity);
                bus.publish(new GameEvent.CityDestroyed(city, attacker));
            }
        } else {
            city.takeDamage(damageToCity);
            bus.publish(new GameEvent.CityAttacked(attacker, city, damageToCity));
        }
    }

    private static boolean shouldCapture(Unit attacker, World world) {
        for (var faction : world.getFactions()) {
            if (String.valueOf(faction.getId()).equals(attacker.getFactionId())) {
                return faction.getCityCount() < 4;
            }
        }
        return true;
    }

    static int calculateDamage(int attack, int defense, UnitRole attackerRole, UnitRole targetRole) {
        int bonus = (attackerRole != null && targetRole != null) ? attackerRole.getAttackBonus(targetRole) : 0;
        int damage = (attack + bonus - defense / 2);
        return Math.max(1, damage);
    }

    private static int calculateDistance(Unit a, Unit b) {
        return HexUtils.getDistance(a.getQ(), a.getR(), b.getQ(), b.getR());
    }
}