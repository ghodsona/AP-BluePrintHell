package com.BluePrintHell.controller;

public enum Screen {
    // اسلش از ابتدای تمام مسیرها حذف شده است
    MAIN_MENU("/com/BluePrintHell/view/screens/MainMenuScreen.fxml"),
    LEVELS("/com/BluePrintHell/view/screens/LevelSelectScreen.fxml"),
    GAME("/com/BluePrintHell/view/screens/GameScreen.fxml"),
    SHOP("/com/BluePrintHell/view/screens/ShopScreen.fxml"),
    SETTINGS("/com/BluePrintHell/view/screens/SettingsScreen.fxml");

    private final String fxmlPath;

    Screen(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}