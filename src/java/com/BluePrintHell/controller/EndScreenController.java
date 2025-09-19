package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.leveldata.LevelData;
import com.BluePrintHell.util.LevelLoader;
import com.BluePrintHell.util.ProgressiveGameBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class EndScreenController {

    @FXML private Label titleLabel;
    @FXML private Label statsLabel;
    @FXML private Button restartButton;
    @FXML private Button nextLevelButton;
    @FXML private Button menuButton;

    private int currentLevel;
    private boolean isWin;

    public void initializeData(GameState gameState, boolean isWin) {
        this.currentLevel = gameState.getLevelNumber();
        this.isWin = isWin;

        if (isWin) {
            titleLabel.setText("LEVEL COMPLETE");
            titleLabel.setStyle("-fx-text-fill: #00f0ff;");

            String stats = String.format("Packets Succeeded: %d\nFinal Coins: %d",
                    gameState.getPacketsSucceeded(), gameState.getPlayerCoins());
            statsLabel.setText(stats);

            nextLevelButton.setManaged(true);
            nextLevelButton.setVisible(true);
            restartButton.setManaged(false);
            restartButton.setVisible(false);

        } else {
            titleLabel.setText("GAME OVER");
            titleLabel.setStyle("-fx-text-fill: #ff3b3b;");

            double lossPercentage = ((double) gameState.getPacketsLost() / gameState.getTotalPacketsSpawned()) * 100;
            String stats = String.format("Packets Succeeded: %d\nPacket Loss: %.1f%%",
                    gameState.getPacketsSucceeded(), lossPercentage);
            statsLabel.setText(stats);

            restartButton.setManaged(true);
            restartButton.setVisible(true);
            nextLevelButton.setManaged(false);
            nextLevelButton.setVisible(false);
        }
    }

    @FXML
    private void onRestartClicked() {
        loadAndStartLevel(currentLevel, false);
    }

    @FXML
    private void onNextLevelClicked() {
        int nextLevelNum = currentLevel + 1;
        loadAndStartLevel(nextLevelNum, true);
    }

    @FXML
    private void onMenuClicked() {
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }

    private void loadAndStartLevel(int levelNumber, boolean progressive) {
        LevelData levelData = LevelLoader.loadLevel(levelNumber);
        if (levelData != null) {
            GameState currentGameState = GameManager.getInstance().getCurrentGameState();
            GameState newGameState;

            if (progressive && currentGameState != null && currentGameState.isProgressiveMode()) {
                newGameState = ProgressiveGameBuilder.buildProgressiveLevel(levelData, currentGameState);
            } else {
                newGameState = ProgressiveGameBuilder.buildProgressiveLevel(levelData, null);
                newGameState.enableProgressiveMode();
            }

            GameManager.getInstance().setCurrentGameState(newGameState);
            ScreenController.getInstance().activate(Screen.GAME);
        } else {
            System.err.println("Failed to load level " + levelNumber + ". Returning to menu.");
            ScreenController.getInstance().activate(Screen.MAIN_MENU);
        }
    }
}