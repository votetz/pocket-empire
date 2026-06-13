package com.pocketempire.events;

import com.pocketempire.entities.Building;
import com.pocketempire.entities.City;
import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import java.util.List;
import com.pocketempire.world.Tile;

import java.util.List;

public sealed interface GameEvent {
    record UnitMoved(Unit unit, int fromQ, int fromR, int toQ, int toR) implements GameEvent {}
    record UnitAttacked(Unit attacker, Unit defender, int damage) implements GameEvent {}
    record UnitDied(Unit unit, Unit killer) implements GameEvent {}
    record UnitHealed(Unit unit, int amount) implements GameEvent {}
    record GameOver(Faction winner, String reason, List<Faction> rankedFactions) implements GameEvent {}
    record TurnStarted(int turn, Faction faction) implements GameEvent {}
    record CounterAttacked(Unit attacker, Unit defender, int damage) implements GameEvent {}
    record UnitSpawned(Unit unit) implements GameEvent {}
    record UnitStateChanged(Unit unit) implements GameEvent {}
    record CityAttacked(Unit attacker, City city, int damage) implements GameEvent {}
    record CityDestroyed(City city, Unit attacker) implements GameEvent {}
    record CityFounded(City city, Unit settler) implements GameEvent {}
    record TileImproved(Tile tile, Unit worker) implements GameEvent {}
    record BuildingBuilt(City city, Building building) implements GameEvent {}
}
