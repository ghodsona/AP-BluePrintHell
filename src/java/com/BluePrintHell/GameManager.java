package com.BluePrintHell;

import com.BluePrintHell.model.GameState;

public class GameManager {
    private static final GameManager instance = new GameManager();
    private GameState currentGameState;

    private GameManager() {}

    public static GameManager getInstance() {
        return instance;
    }

    public void setCurrentGameState(GameState gameState) {
        this.currentGameState = gameState;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }
}