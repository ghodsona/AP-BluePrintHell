package com.BluePrintHell.controller;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class DebugController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // این پیام حیاتی است. اگر چاپ شود یعنی مشکل حل شده.
        System.out.println("!!! SUCCESS: DebugController.initialize() was called!");
    }
}