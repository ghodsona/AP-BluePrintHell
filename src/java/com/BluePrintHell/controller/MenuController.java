package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.util.SaveManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML private Button continueButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SaveManager.hasAutoSave()) {
            continueButton.setManaged(true);
            continueButton.setVisible(true);
        } else {
            continueButton.setManaged(false);
            continueButton.setVisible(false);
        }
    }

    @FXML
    private void onStartGameClicked() {
        // همیشه فایل سیو قبلی را پاک کن تا بازی جدید شروع شود
        SaveManager.deleteAutoSave();
        ScreenController.getInstance().activate(Screen.LEVELS);
    }

    @FXML
    private void onContinueClicked() {
        GameState loadedState = SaveManager.loadGame();
        if (loadedState != null) {
            GameManager.getInstance().setCurrentGameState(loadedState);
            ScreenController.getInstance().activate(Screen.GAME);
        }
    }

    @FXML
    private void onLevelsClicked() {
        SaveManager.deleteAutoSave();
        ScreenController.getInstance().activate(Screen.LEVELS);
    }

    @FXML
    private void onSettingsClicked() {
        ScreenController.getInstance().activate(Screen.SETTINGS);
    }

    @FXML
    private void onExitClicked() {
        Platform.exit();
    }
}