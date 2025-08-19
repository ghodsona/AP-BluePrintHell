package com.BluePrintHell.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // import
import java.util.HashMap;
import java.util.Map;

public class ScreenController {
    private static ScreenController instance;
    private final Stage stage;
    private final StackPane root = new StackPane();
    private final Map<String, Parent> screenCache = new HashMap<>();

    // در فایل ScreenController.java

    private ScreenController(Stage stage) {
        this.stage = stage;
        Scene scene = new Scene(root);

        // مسیر صحیح برای بارگذاری CSS در پروژه ماژولار
        String cssPath = "/com/BluePrintHell/view/styles/app.css";
        URL cssUrl = getClass().getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            // اگر این پیام را در کنسول می‌بینید، یعنی فایل پیدا نشده
            System.err.println("Warning: Stylesheet not found at " + cssPath);
        }

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }

    public void loadScreen(Screen screen) throws IOException {
        if (screen == null) return;

        URL fxmlUrl = getClass().getResource(screen.getFxmlPath()); // <<<< این روش صحیح است
        if (fxmlUrl == null) {
            throw new IOException("Cannot find FXML file: " + screen.getFxmlPath());
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent screenNode = loader.load();
        screenCache.put(screen.name(), screenNode);
    }
    // بقیه متدهای کلاس بدون تغییر باقی می‌مانند...
    public static void initialize(Stage stage) {
        if (instance == null) {
            instance = new ScreenController(stage);
        }
    }
    public static ScreenController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ScreenController has not been initialized...");
        }
        return instance;
    }
    public void activate(Screen screen) {
        if (screen == null) return;
        Parent screenNode = screenCache.get(screen.name());
        if (screenNode == null) {
            System.err.println("Error: Screen '" + screen.name() + "' was not loaded.");
            return;
        }
        root.getChildren().setAll(screenNode);
    }
}