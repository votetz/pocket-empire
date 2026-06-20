package com.pocketempire.events;

import com.pocketempire.config.BuildingConfig;
import com.pocketempire.config.StatusEffectConfig;
import com.pocketempire.tech.TechnologyConfig;
import com.pocketempire.entities.*;

import java.util.List;
import com.pocketempire.world.Tile;

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
    record BuildingBuilt(City city, BuildingConfig building) implements GameEvent {}
    record StatusApplied(Unit unit, StatusEffectConfig effect, int duration) implements GameEvent {}
    record StatusTick(Unit unit, StatusEffectConfig effect, int damage) implements GameEvent {}
    record MageBlinked(Unit unit, int fromQ, int fromR, int toQ, int toR) implements GameEvent {}
    record TriremeRam(Unit attacker, Unit defender, int bonusDamage, int selfDamage) implements GameEvent {}
    record CityCaptured(City city, Unit captor, String oldFactionId) implements GameEvent {}
    record UnitLevelUp(Unit unit, int level) implements GameEvent {}
    record ResearchCompleted(Faction faction, TechnologyConfig tech) implements GameEvent {}
    record WarDeclared(Faction aggressor, Faction target) implements GameEvent {}
    record PeaceDeclared(Faction a, Faction b) implements GameEvent {}
}
