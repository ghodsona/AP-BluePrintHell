package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.leveldata.LevelData;
import com.BluePrintHell.util.GameBuilder;
import com.BluePrintHell.util.LevelLoader;
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

    /**
     * این متد از ScreenController فراخوانی می‌شود تا اطلاعات لازم را به این صفحه منتقل کند.
     * @param gameState وضعیت نهایی بازی
     * @param isWin آیا بازیکن برده است یا باخته
     */
    public void initializeData(GameState gameState, boolean isWin) {
        this.currentLevel = gameState.getLevelNumber();
        this.isWin = isWin;

        if (isWin) {
            // تنظیمات برای حالت برد
            titleLabel.setText("LEVEL COMPLETE");
            titleLabel.setStyle("-fx-text-fill: #00f0ff;"); // رنگ آبی نئونی

            String stats = String.format("Packets Succeeded: %d\nFinal Coins: %d",
                    gameState.getPacketsSucceeded(), gameState.getPlayerCoins());
            statsLabel.setText(stats);

            // نمایش دکمه مرحله بعد
            nextLevelButton.setManaged(true);
            nextLevelButton.setVisible(true);

        } else {
            // تنظیمات برای حالت باخت
            titleLabel.setText("GAME OVER");
            titleLabel.setStyle("-fx-text-fill: #ff3b3b;"); // رنگ قرمز

            double lossPercentage = ((double) gameState.getPacketsLost() / gameState.getTotalPacketsSpawned()) * 100;
            String stats = String.format("Packets Succeeded: %d\nPacket Loss: %.1f%%",
                    gameState.getPacketsSucceeded(), lossPercentage);
            statsLabel.setText(stats);

            // نمایش دکمه تلاش مجدد
            restartButton.setManaged(true);
            restartButton.setVisible(true);
        }
    }

    @FXML
    private void onRestartClicked() {
        // مرحله فعلی را دوباره بارگذاری کن
        System.out.println("Restarting level " + currentLevel + "...");
        loadAndStartLevel(currentLevel);
    }

    @FXML
    private void onNextLevelClicked() {
        // به مرحله بعدی برو
        int nextLevelNum = currentLevel + 1;
        System.out.println("Loading next level " + nextLevelNum + "...");
        loadAndStartLevel(nextLevelNum);
    }

    @FXML
    private void onMenuClicked() {
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }

    private void loadAndStartLevel(int levelNumber) {
        LevelData levelData = LevelLoader.loadLevel(levelNumber);
        if (levelData != null) {
            GameState newGameState = GameBuilder.buildFrom(levelData);
            GameManager.getInstance().setCurrentGameState(newGameState);
            ScreenController.getInstance().activate(Screen.GAME);
        } else {
            // اگر مرحله بعدی وجود نداشت، به منوی اصلی برگرد
            System.err.println("Failed to load level " + levelNumber + ". Returning to menu.");
            ScreenController.getInstance().activate(Screen.MAIN_MENU);
        }
    }
}