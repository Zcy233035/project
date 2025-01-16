package core;

import tileengine.TETile;
import tileengine.Tileset;

public class AutograderBuddy {

    /**
     * Simulates a game, but doesn't render anything or call any StdDraw
     * methods. Instead, returns the world that would result if the input string
     * had been typed on the keyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quit and
     * save. To "quit" in this method, save the game to a file, then just return
     * the TETile[][]. Do not call System.exit(0) in this method.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] getWorldFromInput(String input) {
        World world = new World();
        TETile[][] finalWorldFrame = new TETile[World.WIDTH][World.HEIGHT];

        // Initialize the world with NOTHING tiles
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }

        boolean loadMode = false;
        boolean quitMode = false;

        if (input.length() > 0 && (input.charAt(0) == 'n' || input.charAt(0) == 'N')) {
            StringBuilder seedBuilder = new StringBuilder();
            for (int i = 1; i < input.length(); i++) {
                char c = input.charAt(i);
                if (Character.isDigit(c)) {
                    seedBuilder.append(c);
                } else if (c == 's' || c == 'S') {
                    long seed = Long.parseLong(seedBuilder.toString());
                    world.generateWorld(seed);
                    world.newGame();
                    finalWorldFrame = world.getGeneratedWorld();
                    loadMode = true;
                    break;
                }
            }
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (loadMode) {
                if (c == 'l' || c == 'L') {
                    world.loadGame();
                    finalWorldFrame = world.getGeneratedWorld();
                } else if (c == ':') {
                    quitMode = true;
                } else if (c == 'q' || c == 'Q') {
                    if (quitMode) {
                        world.saveGame();
                        return finalWorldFrame;
                    }
                    quitMode = false;
                } else if (!quitMode) {
                    world.moveAvatar(c);
                    finalWorldFrame = world.getGeneratedWorld();
                }
            }
        }
        return finalWorldFrame;
    }


    /**
     * Used to tell the autograder which tiles are the floor/ground (including
     * any lights/items resting on the ground). Change this
     * method if you add additional tiles.
     */
    public static boolean isGroundTile(TETile t) {
        return t.character() == Tileset.FLOOR.character()
                || t.character() == Tileset.AVATAR.character()
                || t.character() == Tileset.FLOWER.character();
    }

    /**
     * Used to tell the autograder while tiles are the walls/boundaries. Change
     * this method if you add additional tiles.
     */
    public static boolean isBoundaryTile(TETile t) {
        return t.character() == Tileset.WALL.character()
                || t.character() == Tileset.LOCKED_DOOR.character()
                || t.character() == Tileset.UNLOCKED_DOOR.character();
    }
}
