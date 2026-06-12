package com.pocketempire;

import com.pocketempire.simulation.GameSetup;
import com.pocketempire.simulation.GameRunner;

public class Main {
    public static void main(String[] args) {
        GameSetup setup = new GameSetup(50, 15);
        setup.setup();

        GameRunner runner = new GameRunner(setup);
        runner.run();
    }
}
