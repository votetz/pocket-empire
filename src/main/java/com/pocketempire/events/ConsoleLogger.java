package com.pocketempire.events;

import com.pocketempire.config.StatusEffectConfig;
import com.pocketempire.entities.Unit;

public class ConsoleLogger {
    public ConsoleLogger() {
        var bus = GameEventBus.getInstance();
        bus.subscribe(event -> {
            switch (event) {
                case GameEvent.TurnStarted(var turn, var faction) ->
                    System.out.println("\nTurn " + turn + "\nCurrent faction: " + faction.getName());

                case GameEvent.UnitMoved(var unit, var fq, var fr, var tq, var tr) ->
                    System.out.println(unit.getName() + " moved to (" + tq + "," + tr
                            + ") OD left: " + unit.getRemainingOD());

                case GameEvent.UnitDied(var unit, var killer) ->
                    System.out.println(unit.getName() + " has been destroyed!");

                case GameEvent.GameOver(var winner, var reason, var rankings) -> {}

                case GameEvent.UnitAttacked(var attacker, var defender, var damage) ->
                    System.out.println(attacker.getName() + " deals " + damage + " damage to "
                            + defender.getName() + " (" + defender.getHp() + "/" + defender.getMaxHp() + " HP)");

                case GameEvent.CounterAttacked(var attacker, var defender, var damage) ->
                    System.out.println(defender.getName() + " counter-attacks for " + damage + " damage to "
                            + attacker.getName() + " (" + attacker.getHp() + "/" + attacker.getMaxHp() + " HP)");

                case GameEvent.UnitHealed(var unit, var amount) ->
                    System.out.println(unit.getName() + " healed (+" + amount + " HP, "
                            + unit.getHp() + "/" + unit.getMaxHp() + " HP)");

                case GameEvent.UnitSpawned(var unit) -> {
                    String mageInfo = unit.getAbilityType() != null ? " [" + unit.getAbilityType() + "]" : "";
                    System.out.println(unit.getName() + " (" + unit.getUnitType() + mageInfo + ") spawned at (" + unit.getQ() + "," + unit.getR() + ")");
                }

                case GameEvent.UnitStateChanged(var unit) ->
                    System.out.println(unit.getName() + " is now " + unit.getUnitState().name());

                case GameEvent.CityAttacked(var attacker, var city, var damage) ->
                    System.out.println(attacker.getName() + " deals " + damage + " damage to "
                            + city.getName() + " (" + city.getHp() + "/" + city.getMaxHp() + " HP)");

                case GameEvent.CityDestroyed(var city, var attacker) ->
                    System.out.println(city.getName() + " has been destroyed!");

                case GameEvent.CityCaptured(var city, var captor, var oldFactionId) ->
                    System.out.println(city.getName() + " captured by " + captor.getName() + "!");

                case GameEvent.CityFounded(var city, var settler) ->
                    System.out.println(city.getName() + " founded by " + settler.getName());

                case GameEvent.TileImproved(var tile, var worker) ->
                    System.out.println("Tile (" + tile.getQ() + "," + tile.getR() + ") improved by " + worker.getName());

                case GameEvent.BuildingBuilt(var city, var building) ->
                    System.out.println(city.getName() + " built " + building.getName());

                case GameEvent.StatusApplied(Unit unit, StatusEffectConfig effect, int duration) ->
                    System.out.printf("%s %s is now %s for %d turns%n", effect.getIcon(), unit.getName(), effect.getName(), duration);

                case GameEvent.StatusTick(Unit unit, StatusEffectConfig effect, int damage) ->
                    System.out.printf("%s %s takes %d %s damage (%d/%d HP)%n",
                            effect.getIcon(), unit.getName(), damage, effect.getName(),
                            unit.getHp(), unit.getMaxHp());

                case GameEvent.MageBlinked(Unit unit, int fromQ, int fromR, int toQ, int toR) ->
                    System.out.printf("\u2728 %s blinks (%d,%d) \u2192 (%d,%d)%n",
                            unit.getName(), fromQ, fromR, toQ, toR);

                case GameEvent.TriremeRam(Unit attacker, Unit defender, int bonusDamage, int selfDamage) ->
                    System.out.printf("\uD83D\uDC0A %s rams %s (+%d bonus, -%d self) (%d/%d HP)%n",
                            attacker.getName(), defender.getName(), bonusDamage, selfDamage,
                            attacker.getHp(), attacker.getMaxHp());
                case GameEvent.UnitLevelUp(var unit, var level) ->
                    System.out.printf("\u2B50 %s level up to %d (ATK: %d, HP: %d/%d)%n",
                            unit.getName(), level, unit.getAttack(), unit.getHp(), unit.getMaxHp());

                case GameEvent.ResearchCompleted(var faction, var tech) ->
                    System.out.printf("\uD83D\uDD2C %s researched %s%n", faction.getName(), tech.getName());

                case GameEvent.WarDeclared(var aggressor, var target) ->
                    System.out.printf("\u2694\uFE0F %s declared war on %s!%n", aggressor.getName(), target.getName());

                case GameEvent.PeaceDeclared(var a, var b) ->
                    System.out.printf("\uD83D\uDD4A\uFE0F %s and %s made peace%n", a.getName(), b.getName());
            }
        });
    }
}
