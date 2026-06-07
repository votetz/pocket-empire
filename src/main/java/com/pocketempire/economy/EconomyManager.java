package com.pocketempire.economy;

public class EconomyManager {
    private static final int GOLD_PER_CITY = 10;
    private static final int GOLD_PER_UNIT_MAINTENANCE = 2;

    public void processFactionEconomy(com.pocketempire.entities.Faction faction) {
        int income = faction.getCityCount() * GOLD_PER_CITY;
        int maintenance = faction.getUnitCount() * GOLD_PER_UNIT_MAINTENANCE;
        int netProfit = income - maintenance;

        faction.addGold(netProfit);
    }

    public boolean canAffordUnit(com.pocketempire.entities.Faction faction, int cost) {
        return faction.getGold() >= cost;
    }

    public boolean purchaseUnit(com.pocketempire.entities.Faction faction, int cost) {
        if (faction.getGold() < cost) return false;
        faction.spendGold(cost);
        return true;
    }

}
