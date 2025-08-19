package com.BluePrintHell.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void onStartGameClicked() {
        // این دکمه می‌تواند بعدا برای ادامه بازی (Load Game) استفاده شود
        // فعلا آن را به صفحه بازی می‌بریم
        System.out.println("Navigating to Game Screen...");
        ScreenController.getInstance().activate(Screen.GAME);
    }

    @FXML
    private void onLevelsClicked() {
        System.out.println("Navigating to Levels Screen...");
        ScreenController.getInstance().activate(Screen.LEVELS);
    }

    @FXML
    private void onSettingsClicked() {
        System.out.println("Settings button clicked! (Screen not implemented yet)");
        // برای اینکه این کار کند، باید اول Screen.SETTINGS را در Enum تعریف کنید
        // و فایل FXML مربوطه را بسازید.
        ScreenController.getInstance().activate(Screen.SETTINGS);
    }

    @FXML
    private void onExitClicked() {
        System.out.println("Exiting application...");
        Platform.exit(); // بهترین راه برای بستن برنامه JavaFX
    }
}