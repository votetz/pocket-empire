package com.pocketempire.economy;

import com.pocketempire.entities.City;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Map;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

public class EconomyManager {
    private static final int GOLD_PER_CITY = 10;
    private static final int GOLD_PER_UNIT_MAINTENANCE = 2;
    // private static final int GOLD_PER_IMPROVED_TILE = 1;
    private static final int IMPROVED_TILE_RADIUS = 2;

    public void processFactionEconomy(com.pocketempire.entities.Faction faction, World world) {
        int income = faction.getCityCount() * GOLD_PER_CITY;
        int maintenance = faction.getUnitCount() * GOLD_PER_UNIT_MAINTENANCE;
        int improvedTileBonus = countImprovedTilesNearCities(faction, world.getMap());
        int buildingBonus = faction.getCities().stream().mapToInt(City::getGoldBonus).sum();
        int granaryBonus = 0;
        for (City city : faction.getCities()) {
            if (city.getImprovedTileGoldBonus() > 0) {
                int nearCity = countImprovedTilesNearCity(city, world.getMap());
                granaryBonus += nearCity * city.getImprovedTileGoldBonus();
            }
        }

        int netProfit = income - maintenance + improvedTileBonus + buildingBonus + granaryBonus;
        faction.addGold(netProfit);
    }

    private int countImprovedTilesNearCities(com.pocketempire.entities.Faction faction, Map map) {
        int count = 0;
        for (int col = 0; col < map.getWidth(); col++) {
            for (int row = 0; row < map.getHeight(); row++) {
                Tile tile = map.getTileOffSet(col, row);
                if (tile == null || !tile.isImproved()) continue;
                int q = col - (row - (row & 1)) / 2;
                int r = row;
                for (City city : faction.getCities()) {
                    if (HexUtils.getDistance(q, r, city.getQ(), city.getR()) <= IMPROVED_TILE_RADIUS) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    private int countImprovedTilesNearCity(City city, Map map) {
        int count = 0;
        for (int col = 0; col < map.getWidth(); col++) {
            for (int row = 0; row < map.getHeight(); row++) {
                Tile tile = map.getTileOffSet(col, row);
                if (tile == null || !tile.isImproved()) continue;
                int q = col - (row - (row & 1)) / 2;
                int r = row;
                if (HexUtils.getDistance(q, r, city.getQ(), city.getR()) <= IMPROVED_TILE_RADIUS) {
                    count++;
                }
            }
        }
        return count;
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
