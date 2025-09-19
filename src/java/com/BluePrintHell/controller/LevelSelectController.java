package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.leveldata.LevelData;
import com.BluePrintHell.util.LevelLoader;
import com.BluePrintHell.util.ProgressiveGameBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LevelSelectController {

    @FXML
    private void onLevelClicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String levelText = clickedButton.getText().replace("Level ", "");
        int levelNumber = Integer.parseInt(levelText);

        LevelData loadedData = LevelLoader.loadLevel(levelNumber);
        if (loadedData == null) {
            System.err.println("Failed to load level " + levelNumber);
            return;
        }

        GameState currentGameState = GameManager.getInstance().getCurrentGameState();
        GameState newGameState;

        if (currentGameState != null && currentGameState.isProgressiveMode() &&
                levelNumber == currentGameState.getLevelNumber() + 1) {

            newGameState = ProgressiveGameBuilder.buildProgressiveLevel(loadedData, currentGameState);
            System.out.println("Progressive level loaded: Level " + levelNumber);
        } else {
            newGameState = ProgressiveGameBuilder.buildProgressiveLevel(loadedData, null);
            newGameState.enableProgressiveMode();
            System.out.println("New game started from Level " + levelNumber);
        }

        GameManager.getInstance().setCurrentGameState(newGameState);
        ScreenController.getInstance().activate(Screen.GAME);
    }

    @FXML
    private void onBackToMenuClicked() {
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }
}