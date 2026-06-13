package com.pocketempire.events;

import com.pocketempire.entities.StatusEffect;
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

                case GameEvent.UnitSpawned(var unit) ->
                    System.out.println(unit.getName() + " (" + unit.getUnitType() + ") spawned at (" + unit.getQ() + "," + unit.getR() + ")");

                case GameEvent.UnitStateChanged(var unit) ->
                    System.out.println(unit.getName() + " is now " + unit.getUnitState().name());

                case GameEvent.CityAttacked(var attacker, var city, var damage) ->
                    System.out.println(attacker.getName() + " deals " + damage + " damage to "
                            + city.getName() + " (" + city.getHp() + "/" + city.getMaxHp() + " HP)");

                case GameEvent.CityDestroyed(var city, var attacker) ->
                    System.out.println(city.getName() + " has been destroyed!");

                case GameEvent.CityFounded(var city, var settler) ->
                    System.out.println(city.getName() + " founded by " + settler.getName());

                case GameEvent.TileImproved(var tile, var worker) ->
                    System.out.println("Tile (" + tile.getQ() + "," + tile.getR() + ") improved by " + worker.getName());

                case GameEvent.BuildingBuilt(var city, var building) ->
                    System.out.println(city.getName() + " built " + building.getName());
                case GameEvent.StatusApplied(Unit unit, StatusEffect effect, int duration) -> {
                    String icon = switch (effect) {
                        case BURNING -> "\uD83D\uDD25";
                        case FROZEN -> "\u2744\uFE0F";
                        case POISONED -> "\uD83E\uDDEA";
                        case STUNNED -> "\uD83D\uDCA3";
                    };
                    System.out.printf(" %s %s is now %s for %d turns%n", icon, unit.getName(), effect.name(), duration);
                }
            }
        });
    }
}
