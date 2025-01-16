package core;

import tileengine.TETile;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public int avatarX;
    public int avatarY;
    public int avatarX2;
    public int avatarY2;
    public char[][] worldGrid;
    public ArrayList<Point> floorTiles;
    public long seed;
    public ArrayList<Rectangle> rooms;
    public ArrayList<Point> coins;

    public GameState(int avatarX, int avatarY, int avatarX2, int avatarY2, char[][] worldGrid, ArrayList<Point> floorTiles, long seed, ArrayList<Rectangle> rooms, ArrayList<Point> coins) {
        this.avatarX = avatarX;
        this.avatarY = avatarY;
        this.avatarX2 = avatarX2;
        this.avatarY2 = avatarY2;
        this.worldGrid = worldGrid;
        this.floorTiles = floorTiles;
        this.seed = seed;
        this.rooms = rooms;
        this.coins = coins;
    }
}