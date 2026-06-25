package com.pocketempire.events;

import com.pocketempire.entities.Faction;
import com.pocketempire.entities.Unit;
import com.pocketempire.tiles.TileType;
import com.pocketempire.world.HexUtils;
import com.pocketempire.world.Map;
import com.pocketempire.world.Tile;
import com.pocketempire.world.World;

import java.util.List;
import java.util.Random;

public class EventManager {
    private final World world;
    private final Random rng = new Random();
    private final ForestFireConfig config;

    public EventManager(World world) {
        this.world = world;
        this.config = EventConfigLoader.getForestFireConfig();
    }

    public void processEvents(Faction faction) {
        Map map = world.getMap();
        for (int q = 0; q < map.getWidth(); q++) {
            for (int r = 0; r < map.getHeight(); r++) {
                Tile tile = map.getTile(q, r);
                if (tile == null) continue;
                processForestFire(tile, faction);
                processBurning(tile, faction);
                processRecovery(tile);
            }
        }
    }

    private void processForestFire(Tile tile, Faction faction) {
        if (tile.getType() != TileType.FOREST) return;
        if (tile.isBurning()) return;
        if (rng.nextDouble() >= config.getChance()) return;

        tile.setBurningTurns(config.getBurnDuration());
        GameEventBus.getInstance().publish(new GameEvent.ForestFireStarted(tile.getQ(), tile.getR()));
        applyBurnDamage(tile, faction);
        spreadFire(tile);
    }

    private void processBurning(Tile tile, Faction faction) {
        if (!tile.isBurning()) return;

        applyBurnDamage(tile, faction);
        spreadFire(tile);

        tile.setBurningTurns(tile.getBurningTurns() - 1);
        if (tile.getBurningTurns() == 0) {
            tile.setType(TileType.SCORCHED_EARTH);
            tile.setRecoverTurns(config.getRecoverDuration());
            GameEventBus.getInstance().publish(new GameEvent.ForestBurnt(tile.getQ(), tile.getR()));
        }
    }

    private void processRecovery(Tile tile) {
        if (!tile.isScorched()) return;
        if (tile.getRecoverTurns() <= 0) return;

        tile.setRecoverTurns(tile.getRecoverTurns() - 1);
        if (tile.getRecoverTurns() == 0) {
            tile.setType(TileType.FOREST);
            GameEventBus.getInstance().publish(new GameEvent.ForestRecovered(tile.getQ(), tile.getR()));
        }
    }

    private void applyBurnDamage(Tile tile, Faction faction) {
        for (Unit unit : faction.getUnits()) {
            if (!unit.isAlive()) continue;
            if (unit.getQ() == tile.getQ() && unit.getR() == tile.getR()) {
                unit.setHp(unit.getHp() - config.getDamagePerTurn());
                GameEventBus.getInstance().publish(new GameEvent.ForestFireDamage(unit, config.getDamagePerTurn()));
            }
        }
    }

    private void spreadFire(Tile tile) {
        for (int[] dir : HexUtils.DIRECTIONS) {
            int nq = tile.getQ() + dir[0];
            int nr = tile.getR() + dir[1];
            if (!world.getMap().isInBounds(nq, nr)) continue;

            Tile neighbor = world.getMap().getTile(nq, nr);
            if (neighbor == null) continue;
            if (neighbor.getType() != TileType.FOREST) continue;
            if (neighbor.isBurning()) continue;

            if (rng.nextDouble() < config.getSpreadChance()) {
                neighbor.setBurningTurns(config.getBurnDuration());
                GameEventBus.getInstance().publish(new GameEvent.ForestFireSpread(nq, nr));
            }
        }
    }
}
