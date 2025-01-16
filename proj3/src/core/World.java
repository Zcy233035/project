package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class World {
    public static final int WIDTH = 60;
    public static final int HEIGHT = 50;
    private static final int ROOM_MAX_SIZE = 8;
    private static final int ROOM_MIN_SIZE = 4;
    private static final int MAX_ROOMS = 15;
    private TETile[][] worldGrid;
    ///for view
    private TERenderer ter;
    //
    private int avatarX, avatarY;
    public int avatarX2, avatarY2;
    private ArrayList<Point> floorTiles;
    public ArrayList<Point> coins;
    public long seed;
    public boolean mainQuitFlag = false;
    public boolean quitFlag1 = false;
    public boolean quitflag2 = false;
    public boolean loadFlag = false;
    public int coinX;
    public int coinY;
    public int coinCounter1;
    public int coinCounter2;
    public int totalCoin;
    public int stepCounter1 = 0;
    public int stepCounter2 = 0;
    public ArrayList<Rectangle> rooms;
    public boolean turnOffSign = false;


    public World() {
        worldGrid = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                worldGrid[x][y] = Tileset.NOTHING;
            }
        }
        coins = new ArrayList<>();
        coinCounter1 = 0;
        coinCounter2 = 0;
        totalCoin = 0;
        ////for view
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
    }

    public void welcome() {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 4) * 3, "CS61B: THE GAME");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        int centerWidth = WIDTH / 2;
        int centerHeight = HEIGHT / 2;
        StdDraw.text(centerWidth, centerHeight, "New Game (N)");
        StdDraw.text(centerWidth, centerHeight - 5, "Load Game (L)");
        StdDraw.text(centerWidth, centerHeight - 10, "Quit (Q)");
        StdDraw.show();
        StdDraw.pause(1000);
        handleUserInput();
    }

    private void handleUserInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'n' || c == 'N') {
                    newGame();
                    break;
                } else if (c == 'l' || c == 'L') {
                    loadGame();
                    break;
                } else if (c == 'q' || c == 'Q') {
                    mainQuitFlag = true;
                    return;
                } else {
                    showInvalidInputMessage();
                    break;
                }
            }
        }
    }

    private void showInvalidInputMessage() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.RED);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Invalid input, please try again.");
        StdDraw.show();
        StdDraw.pause(1000);
        welcome();  // Re-show the welcome screen after the pause
    }

    public void newGame() {
        seed = getSeedFromUser();
        Random random = new Random(seed); // 使用同一个 Random 对象
        rooms = generateWorld(seed); // 传入同一个 Random 对象和房间信息
        placeAvatar(random); // 传入同一个 Random 对象
        placeAvatar1(random);
        ///for view
        placeCoin(random, rooms);
        ter.renderFrame(worldGrid, coins, rooms, turnOffSign);
        gameLoop();
    }
//    public void newGame(long seed) {
//        // 使用固定的种子值或从输入中获取种子
//         this.seed = seed;
//        Random random = new Random(seed);
//        rooms = generateWorld(seed);
//        placeAvatar(random);
//        placeAvatar1(random);
//        placeCoin(random, rooms);
//        ter.renderFrame(worldGrid, coins, rooms, turnOffSign);  // 注释掉渲染代码
//        gameLoop();  // 注释掉游戏循环
//    }



    public void placeAvatar(Random random) {
        if (floorTiles.isEmpty()) {
            throw new IllegalStateException("No floor tiles available to place avatar.");
        }
        Point avatarPosition = floorTiles.get(random.nextInt(floorTiles.size()));
        floorTiles.remove(avatarPosition);
        avatarX = avatarPosition.x;
        avatarY = avatarPosition.y;
        worldGrid[avatarX][avatarY] = Tileset.AVATAR;
    }

    public void placeAvatar1(Random random) {
        if (floorTiles.isEmpty()) {
            throw new IllegalStateException("No floor tiles available to place avatar.");
        }
        Point avatarPosition = floorTiles.get(random.nextInt(floorTiles.size()));
        floorTiles.remove(avatarPosition);
        avatarX2 = avatarPosition.x;
        avatarY2 = avatarPosition.y;
        worldGrid[avatarX2][avatarY2] = Tileset.AVATAR2;
    }




    public void placeCoin(Random random, ArrayList<Rectangle> rooms) {
        for (Rectangle room : rooms) {
            int coinX = random.nextInt(room.width - 2) + room.x + 1; // 确保 coin 不放置在墙上
            int coinY = random.nextInt(room.height - 2) + room.y + 1;
            coins.add(new Point(coinX, coinY));
            worldGrid[coinX][coinY] = Tileset.TREE;
        }
    }


    public void checkIfCollect() {
        if (totalCoin == rooms.size()) {
            totalCoin = 0;
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 30);
            StdDraw.setFont(fontBig);
            StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 6) * 5, "You two have collect all the coins!");
            String message = "The steps player1 have made: " + stepCounter1;
            StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 6) * 4, message);
            String message1 = "The coins player1 have collected: " + coinCounter1;
            StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 6) * 3, message1);
            String message2 = "The steps player2 have made: " + stepCounter2;
            StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 6) * 2, message2);
            String message3 = "The coins player2 have collected: " + coinCounter2;
            StdDraw.text(this.WIDTH / 2, (this.HEIGHT / 6) * 1, message3);
            StdDraw.show();
            StdDraw.pause(5000);
            ter = new TERenderer();
            ter.initialize(WIDTH, HEIGHT);
            ter.renderFrame(worldGrid, coins, rooms, turnOffSign);
        }
    }

    private final Object lock = new Object();
    private boolean mouseMoved = false;


    private void gameLoop() {
        int prevHighlightX = -1;
        int prevHighlightY = -1;

        while (true) {
            // 处理键盘输入
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                moveAvatar(key);
                if (quitflag2) {
                    saveGame();
                    quitflag2 = false;
                    return;
                }
                ter.renderFrame(worldGrid, coins, rooms, turnOffSign);

            }

            // 获取当前鼠标位置
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();

            // 检查鼠标是否移动
            if (mouseX != prevHighlightX || mouseY != prevHighlightY) {
                // 清除之前的高亮显示
                if (prevHighlightX != -1 && prevHighlightY != -1) {
                    TETile prevTile = worldGrid[prevHighlightX][prevHighlightY];
                    StdDraw.setPenColor(prevTile.textColor());
                    StdDraw.filledRectangle(prevHighlightX + 0.5, prevHighlightY + 0.5, 0.5, 0.5);
                    prevTile.draw(prevHighlightX, prevHighlightY);
                }

                // 显示新的高亮
                display(mouseX, mouseY);
                StdDraw.show();

                // 更新之前的鼠标位置
                prevHighlightX = mouseX;
                prevHighlightY = mouseY;
            }
            checkIfCollect();
        }
    }

    private void display(int mouseX, int mouseY) {
        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
            StdDraw.setPenColor(new Color(255, 255, 0, 128)); // 半透明黄色
            StdDraw.filledRectangle(mouseX + 0.5, mouseY + 0.5, 0.5, 0.5); // 绘制半透明矩形

            TETile tile = worldGrid[mouseX][mouseY];
            String description = tile.description();

            // 清除之前的HUD显示
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.filledRectangle(WIDTH / 2, HEIGHT - 0.3, WIDTH / 2, 1.5);

            // 显示描述信息
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.textLeft(1, HEIGHT - 1, "Tile: " + description);
        }
    }

    void moveAvatar(char key) {
        int newX = avatarX;
        int newY = avatarY;
        int newX2 = avatarX2;
        int newY2 = avatarY2;
        if (quitFlag1) {
            if (key == 'q' || key == 'Q') {
                quitflag2 = true;
            } else {
                quitFlag1 = false;
                return;
            }
        }
        switch (key) {
            case 'W':
            case 'w':
                newY += 1;
                quitFlag1 = false;
                break;
            case 'A':
            case 'a':
                newX -= 1;
                quitFlag1 = false;
                break;
            case 'S':
            case 's':
                newY -= 1;
                quitFlag1 = false;
                break;
            case 'D':
            case 'd':
                newX += 1;
                quitFlag1 = false;
                break;
            case ':':
                quitFlag1 = true;
                return;
            case 'l':
                loadFlag = true;
                break;
            case 't':
            case 'T':
                turnOffSign = true;
                break;
            case 'o':
            case 'O':
                turnOffSign = false;
                break;
            case 'u':
            case 'U':
                newY2 += 1;
                break;
            case 'h':
            case 'H':
                newX2 -= 1;
                break;
            case 'j':
            case 'J':
                newY2 -= 1;
                break;
            case 'k':
            case 'K':
                newX2 += 1;
                break;
            default:
                return; // Ignore other keys
        }

        // Check if the new position is valid (not a wall or out of bounds)
        if (isValidMove(newX, newY) && (newX != avatarX || newY != avatarY)) {
            worldGrid[avatarX][avatarY] = Tileset.FLOOR; // Clear old position
            avatarX = newX;
            avatarY = newY;
            if (worldGrid[avatarX][avatarY] == Tileset.TREE) {
                coinCounter1 += 1;
                totalCoin += 1;
            }
            stepCounter1 += 1;
            worldGrid[avatarX][avatarY] = Tileset.AVATAR;// Place avatar in new position
            ter.renderFrame(worldGrid, coins, rooms, turnOffSign);
        }

        if (isValidMove(newX2, newY2) && (newX2 != avatarX2 || newY2 != avatarY2)) {
            worldGrid[avatarX2][avatarY2] = Tileset.FLOOR; // Clear old position
            avatarX2 = newX2;
            avatarY2 = newY2;
            if (worldGrid[avatarX2][avatarY2] == Tileset.TREE) {
                coinCounter2 += 1;
                totalCoin += 1;
            }
            stepCounter2 += 1;
            worldGrid[avatarX2][avatarY2] = Tileset.AVATAR2;// Place avatar in new position
            ter.renderFrame(worldGrid, coins, rooms, turnOffSign);
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && worldGrid[x][y] != Tileset.WALL;
    }

    public void saveGame() {
        char[][] charWorldGrid = new char[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                charWorldGrid[x][y] = worldGrid[x][y].character();
            }
        }
        GameState gameState = new GameState(avatarX, avatarY, avatarX2, avatarY2, charWorldGrid, floorTiles, seed, rooms, coins);
        try (FileOutputStream fileOut = new FileOutputStream("gameState.ser");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(gameState);
            System.out.println("Game state saved to: " + new File("gameState.ser").getAbsolutePath());
        } catch (IOException i) {
            i.printStackTrace();
        }
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Quit and saved the game");
        StdDraw.show();
        StdDraw.pause(1000);
        //for the view
        System.exit(0);
    }


    // 加载游戏状态的方法
    public void loadGame() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Loading Game...");
        StdDraw.show();
        try (FileInputStream fileIn = new FileInputStream("gameState.ser");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            GameState gameState = (GameState) in.readObject();
            this.avatarX = gameState.avatarX;
            this.avatarY = gameState.avatarY;
            this.avatarX2 = gameState.avatarX2;
            this.avatarY2 = gameState.avatarY2;
            this.seed = gameState.seed;
            this.rooms = gameState.rooms;
            this.coins = gameState.coins;

            // 还原worldGrid
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    worldGrid[x][y] = charToTETile(gameState.worldGrid[x][y]);
                }
            }

            System.out.println("Game state loaded.");
            reinitializeRenderer();
            ter.renderFrame(worldGrid, coins, rooms, turnOffSign); // 确保包含光效渲染
            gameLoop();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    // 重新初始化渲染器的方法
    private void reinitializeRenderer() {
        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
    }

    // 将char转换为TETile的方法
    private TETile charToTETile(char c) {
        switch (c) {
            case '#':
                return Tileset.WALL;
            case '·':
                return Tileset.FLOOR;
            case 'W':
                return Tileset.AVATAR;
            case 'Z':
                return Tileset.AVATAR2;
            case '♠':
                return Tileset.TREE;
            default:
                return Tileset.NOTHING;
        }
    }



    private long getSeedFromUser() {
        StringBuilder seedStr = new StringBuilder();
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Enter a random seed, then press 'S' to start:");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 's' || c == 'S') {
                    break;
                } else if (Character.isDigit(c)) {
                    seedStr.append(c);
                    StdDraw.clear(Color.BLACK);
                    StdDraw.setPenColor(Color.WHITE);
                    StdDraw.text(WIDTH / 2, HEIGHT / 2, "Enter a random seed, then press 'S' to start:");
                    StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, seedStr.toString());
                    StdDraw.show();
                }
            }
        }
        return Long.parseLong(seedStr.toString());
    }

    public ArrayList<Rectangle> generateWorld(long seed) {
        Random random = new Random(seed);
        floorTiles = new ArrayList<>();

        // List to store room coordinates and sizes
        ArrayList<Rectangle> rooms = new ArrayList<>();

        // Generate random rooms
        for (int i = 0; i < MAX_ROOMS; i++) {
            int roomWidth = random.nextInt(ROOM_MAX_SIZE - ROOM_MIN_SIZE + 1) + ROOM_MIN_SIZE;
            int roomHeight = random.nextInt(ROOM_MAX_SIZE - ROOM_MIN_SIZE + 1) + ROOM_MIN_SIZE;
            int x = random.nextInt(WIDTH - roomWidth - 1);
            int y = random.nextInt(HEIGHT - roomHeight - 1);

            Rectangle newRoom = new Rectangle(x, y, roomWidth, roomHeight);

            boolean overlaps = false;
            for (Rectangle room : rooms) {
                if (newRoom.intersects(room)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                rooms.add(newRoom);
                createRoom(newRoom);
            }
        }

        // Connect rooms with hallways
        for (int i = 1; i < rooms.size(); i++) {
            Rectangle roomA = rooms.get(i - 1);
            Rectangle roomB = rooms.get(i);
            connectRooms(roomA, roomB, random);
        }

        return rooms; // 返回房间信息
    }


    private void createRoom(Rectangle room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                if (x == room.x || x == room.x + room.width - 1 || y == room.y || y == room.y + room.height - 1) {
                    worldGrid[x][y] = Tileset.WALL;
                } else {
                    worldGrid[x][y] = Tileset.FLOOR;
                    floorTiles.add(new Point(x, y));
                }
            }
        }
    }

    private void connectRooms(Rectangle roomA, Rectangle roomB, Random random) {
        int x1 = roomA.x + roomA.width / 2;
        int y1 = roomA.y + roomA.height / 2;
        int x2 = roomB.x + roomB.width / 2;
        int y2 = roomB.y + roomB.height / 2;

        if (random.nextBoolean()) {
            createHorizontalHallway(x1, x2, y1);
            createVerticalHallway(y1, y2, x2);
        } else {
            createVerticalHallway(y1, y2, x1);
            createHorizontalHallway(x1, x2, y2);
        }
    }

    private void createHorizontalHallway(int x1, int x2, int y) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            if (worldGrid[x][y] != Tileset.FLOOR) {
                worldGrid[x][y] = Tileset.FLOOR;
            }
            if (worldGrid[x][y + 1] != Tileset.FLOOR) {
                worldGrid[x][y + 1] = Tileset.WALL;
            }
            if (worldGrid[x][y - 1] != Tileset.FLOOR) {
                worldGrid[x][y - 1] = Tileset.WALL;
            }
        }
    }

    private void createVerticalHallway(int y1, int y2, int x) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            if (worldGrid[x][y] != Tileset.FLOOR) {
                worldGrid[x][y] = Tileset.FLOOR;
            }
            if (worldGrid[x + 1][y] != Tileset.FLOOR) {
                worldGrid[x + 1][y] = Tileset.WALL;
            }
            if (worldGrid[x - 1][y] != Tileset.FLOOR) {
                worldGrid[x - 1][y] = Tileset.WALL;
            }
        }
    }
    //part of the room create code is generated by LLM


    public TETile[][] getGeneratedWorld() {
        return worldGrid;
    }
}
