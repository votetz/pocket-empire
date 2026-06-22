package com.pocketempire.diplomacy;

import com.pocketempire.entities.Faction;
import com.pocketempire.events.GameEvent;
import com.pocketempire.events.GameEventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiplomacyManager {
    private static final int WAR_COOLDOWN = 10;
    private static final int WAR_REPUTATION = -50;

    private final Map<Integer, Map<Integer, Integer>> reputation = new HashMap<>();
    private final Map<String, Integer> warCooldowns = new HashMap<>();
    private final Map<String, Integer> warStartTurn = new HashMap<>();
    private final Map<String, Map<Integer, Integer>> citiesAtWarStart = new HashMap<>();

    public DiplomacyManager() {}

    public void init(List<Faction> factions) {
        for (Faction a : factions) {
            for (Faction b : factions) {
                if (a.getId() != b.getId()) {
                    int baseRep = 0;
                    if (a.getConfig() != null) {
                        baseRep += a.getConfig().getStartingReputation();
                    }
                    if (b.getConfig() != null) {
                        baseRep += b.getConfig().getStartingReputation();
                    }
                    setReputation(a.getId(), b.getId(), baseRep);
                }
            }
        }
    }

    public int getReputation(int factionA, int factionB) {
        if (factionA == factionB) return 100;
        return reputation
                .getOrDefault(factionA, Map.of())
                .getOrDefault(factionB, 0);
    }

    public DiplomaticStatus getStatus(int factionA, int factionB) {
        return DiplomaticStatus.fromValue(getReputation(factionA, factionB));
    }

    public boolean isHostile(int factionA, int factionB) {
        return getStatus(factionA, factionB) == DiplomaticStatus.WAR;
    }

    public boolean isAllied(int factionA, int factionB) {
        DiplomaticStatus status = getStatus(factionA, factionB);
        return status == DiplomaticStatus.ALLIED || status == DiplomaticStatus.DEVOTED;
    }

    public void modifyReputation(int factionA, int factionB, int delta) {
        int currentA = getReputation(factionA, factionB);
        int newVal = Math.max(-100, Math.min(100, currentA + delta));
        setReputation(factionA, factionB, newVal);
        setReputation(factionB, factionA, newVal);
    }

    public void declareWar(Faction aggressor, Faction target, int currentTurn, String reason) {
        if (isHostile(aggressor.getId(), target.getId())) return;

        String key = cooldownKey(aggressor.getId(), target.getId());
        if (warCooldowns.getOrDefault(key, 0) > 0) return;

        modifyReputation(aggressor.getId(), target.getId(), WAR_REPUTATION);
        warCooldowns.put(key, WAR_COOLDOWN);
        warStartTurn.put(key, currentTurn);
        citiesAtWarStart.put(key, Map.of(
                aggressor.getId(), aggressor.getCityCount(),
                target.getId(), target.getCityCount()
        ));

        GameEventBus.getInstance().publish(new GameEvent.WarDeclared(aggressor, target, reason));
    }

    public void makePeace(Faction a, Faction b) {
        if (!isHostile(a.getId(), b.getId())) return;

        String key = cooldownKey(a.getId(), b.getId());
        warStartTurn.remove(key);
        citiesAtWarStart.remove(key);

        setReputation(a.getId(), b.getId(), 0);
        setReputation(b.getId(), a.getId(), 0);

        GameEventBus.getInstance().publish(new GameEvent.PeaceDeclared(a, b));
    }

    public void tickCooldowns() {
        warCooldowns.replaceAll((key, value) -> Math.max(0, value - 1));
    }

    public int getWarDuration(int factionA, int factionB, int currentTurn) {
        String key = cooldownKey(factionA, factionB);
        Integer start = warStartTurn.get(key);
        return start != null ? currentTurn - start : 0;
    }

    public int getCitiesLostInWar(int factionA, int factionB, int currentCities) {
        String key = cooldownKey(factionA, factionB);
        Map<Integer, Integer> atStart = citiesAtWarStart.get(key);
        if (atStart == null) return 0;
        int citiesAtStart = atStart.getOrDefault(factionA, currentCities);
        return Math.max(0, citiesAtStart - currentCities);
    }

    private void setReputation(int factionA, int factionB, int value) {
        reputation.computeIfAbsent(factionA, k -> new HashMap<>()).put(factionB, value);
    }

    private String cooldownKey(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    public void formAlliance(Faction a, Faction b) {
        if (isHostile(a.getId(), b.getId())) return;
        setReputation(a.getId(), b.getId(), 45);
        setReputation(b.getId(), a.getId(), 45);
        GameEventBus.getInstance().publish(new GameEvent.AllianceFormed(a, b));
    }
}
