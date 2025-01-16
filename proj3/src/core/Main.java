package core;
import edu.princeton.cs.algs4.StdDraw;

public class Main {
    public static void main(String[] args) {
        // build your own world!
        World myWorld = new World();
        myWorld.welcome();
        if(myWorld.mainQuitFlag) {
            System.exit(0);
        }

    }
}


