package com.pocketempire.diplomacy;

import com.pocketempire.config.CasusBelliConfig;
import com.pocketempire.config.CasusBelliConfigLoader;
import com.pocketempire.entities.Faction;
import com.pocketempire.world.World;

import java.util.*;

public class CasusBelliManager {
    private final Map<String, CasusBelli> reasons = new HashMap<>();
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private final List<CasusBelli> orderedReasons = new ArrayList<>();

    public CasusBelliManager() {
        register(new com.pocketempire.diplomacy.impl.BorderIncursion());
        register(new com.pocketempire.diplomacy.impl.TerritorialDispute());
        register(new com.pocketempire.diplomacy.impl.WeakNeighbor());
        register(new com.pocketempire.diplomacy.impl.MilitaryDominance());
    }

    private void register(CasusBelli cb) {
        reasons.put(cb.getId(), cb);
        orderedReasons.add(cb);
    }

    public CasusBelli findReason(Faction a, Faction b, World world) {
        for (CasusBelli cb : orderedReasons) {
            if (isOnCooldown(cb.getId(), a.getId(), b.getId())) continue;
            if (cb.check(a, b, world)) return cb;
        }
        return null;
    }

    public void onWarDeclared(String reasonId, Faction a, Faction b, int currentTurn) {
        CasusBelliConfig config = CasusBelliConfigLoader.getConfig(reasonId);
        int cooldown = config != null ? config.getCooldown() : 10;
        cooldowns.put(cooldownKey(a.getId(), b.getId(), reasonId), currentTurn + cooldown);
    }

    private boolean isOnCooldown(String reasonId, int factionA, int factionB) {
        String key = cooldownKey(factionA, factionB, reasonId);
        Integer expiresAt = cooldowns.get(key);
        return expiresAt != null; // turn check happens externally
    }

    public void tickCooldowns(int currentTurn) {
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTurn);
    }

    private String cooldownKey(int a, int b, String reasonId) {
        return Math.min(a, b) + "-" + Math.max(a, b) + ":" + reasonId;
    }
}
