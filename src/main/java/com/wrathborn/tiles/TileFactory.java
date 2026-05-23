package com.wrathborn.tiles;

public class TileFactory {
    public static Tile createTile(TileType type) {
        return new Tile(type);
    }

    public static class Tile {
        private final TileType type;

        private Tile(TileType type) {
            this.type = type;
        }

        public TileType getType() {
            return type;
        }

        public double getMovementSpeed() {
            return type.movementSpeed;
        }

        public boolean isImpassable() {
            return type.blocksMovement;
        }

        public char getSymbol() {
            return type.symbol;
        }

        @Override
        public String toString() {
            return String.valueOf(type.symbol);
        }
    }
}
