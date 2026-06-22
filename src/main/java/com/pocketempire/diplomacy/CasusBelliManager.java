package com.pocketempire.diplomacy;

import com.pocketempire.config.CasusBelliConfig;
import com.pocketempire.config.CasusBelliConfigLoader;
import com.pocketempire.entities.Faction;
import com.pocketempire.world.World;

import java.util.*;

public class CasusBelliManager {
    private static final int PEACE_PERIOD = 20;
    private static final int REPUTATION_THRESHOLD = -30;

    private final Map<String, CasusBelli> reasons = new HashMap<>();
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private final List<CasusBelli> orderedReasons = new ArrayList<>();

    public CasusBelliManager() {
        register(new BorderIncursion());
        register(new TerritorialDispute());
        register(new WeakNeighbor());
        register(new MilitaryDominance());
    }

    private void register(CasusBelli cb) {
        reasons.put(cb.getId(), cb);
        orderedReasons.add(cb);
    }

    public CasusBelli findReason(Faction a, Faction b, World world, DiplomacyManager dm, int currentTurn) {
        if (currentTurn < PEACE_PERIOD) return null;
        if (dm.getReputation(a.getId(), b.getId()) > REPUTATION_THRESHOLD) return null;

        for (CasusBelli cb : orderedReasons) {
            if (isOnCooldown(cb.getId(), a.getId(), b.getId())) continue;
            if (cb.check(a, b, world, currentTurn)) return cb;
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
        return expiresAt != null;
    }

    public void tickCooldowns(int currentTurn) {
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTurn);
    }

    private String cooldownKey(int a, int b, String reasonId) {
        return Math.min(a, b) + "-" + Math.max(a, b) + ":" + reasonId;
    }
}
