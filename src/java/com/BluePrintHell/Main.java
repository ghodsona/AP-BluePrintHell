package com.BluePrintHell;

import com.BluePrintHell.controller.Screen;
import com.BluePrintHell.controller.ScreenController;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            ScreenController.initialize(primaryStage);
            ScreenController sc = ScreenController.getInstance();

            for (Screen screen : Screen.values()) {
                sc.loadScreen(screen);
            }

            sc.activate(Screen.MAIN_MENU);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load application screens.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}