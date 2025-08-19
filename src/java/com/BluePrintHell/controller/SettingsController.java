package com.BluePrintHell.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private Slider volumeSlider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: مقدار اولیه اسلایدر را از یک کلاس مدیریت تنظیمات بخوانید
        // volumeSlider.setValue(SettingsManager.getInstance().getVolume());
        System.out.println("Settings screen initialized.");
    }

    @FXML
    private void onSaveAndBackClicked() {
        double volumeValue = volumeSlider.getValue();
        System.out.println("Saving volume: " + volumeValue);

        // TODO: مقدار جدید صدا را در یک کلاس مدیریت تنظیمات ذخیره کنید
        // SettingsManager.getInstance().setVolume(volumeValue);

        System.out.println("Returning to Main Menu...");
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }
}