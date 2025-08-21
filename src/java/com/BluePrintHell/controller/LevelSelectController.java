package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.leveldata.LevelData; // import
import com.BluePrintHell.util.GameBuilder;
import com.BluePrintHell.util.LevelLoader; // import
import javafx.event.ActionEvent;
import javafx.fxml.FXML; 
import javafx.scene.control.Button;

public class LevelSelectController {

    @FXML
    private void onLevelClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String levelText = clickedButton.getText().replace("Level ", "");
        int levelNumber = Integer.parseInt(levelText);

        System.out.println("Loading Level " + levelNumber + "...");

        // مرحله را از فایل JSON بارگذاری کن
        LevelData loadedData = LevelLoader.loadLevel(levelNumber);

        if (loadedData != null) {
            System.out.println("Level '" + loadedData.getLevelName() + "' loaded successfully!");
            System.out.println("Initial coins: " + loadedData.getInitialCoins());
            System.out.println("Number of systems: " + loadedData.getSystems().size());
            GameState newGameState = GameBuilder.buildFrom(loadedData);

            System.out.println("2. GameState built with " + newGameState.getSystems().size() + " systems.");
            // ۲. وضعیت ساخته شده را در GameManager قرار بده
            GameManager.getInstance().setCurrentGameState(newGameState);

            // TODO: در قدم بعدی، این دیتا را به GameController پاس می‌دهیم تا بازی را بسازد

            ScreenController.getInstance().activate(Screen.GAME);
        } else {
            System.err.println("Failed to load level " + levelNumber);
        }
    }

    @FXML
    private void onBackToMenuClicked() {
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }
}