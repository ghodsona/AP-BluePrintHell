package com.BluePrintHell.view;

import com.BluePrintHell.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// این کلاس ظاهر صفحه بازی را می‌سازد
public class GameScreenView extends BorderPane {

    private Canvas gameCanvas;
    private Label coinsLabel;
    private Label wireLengthLabel;
    private Label packetLossLabel;

    public GameScreenView(GameController controller) {
        // ۱. ساختن تمام اجزای گرافیکی
        gameCanvas = new Canvas(1200, 800); // اندازه اولیه
        coinsLabel = new Label("Coins: 0");
        wireLengthLabel = new Label("Wire Length: 1000");
        packetLossLabel = new Label("Packet Loss: 0%");

        Button shopButton = new Button("Shop");
        Button pauseButton = new Button("Pause");
        Button menuButton = new Button("Menu");
        Button runButton = new Button("Run"); // << دکمه جدید

        // ۲. اعمال استایل‌ها
        this.getStyleClass().add("root-pane");
        coinsLabel.getStyleClass().add("hud-label");
        wireLengthLabel.getStyleClass().add("hud-label");
        packetLossLabel.getStyleClass().add("hud-label");
        shopButton.getStyleClass().add("button");
        pauseButton.getStyleClass().add("button");
        menuButton.getStyleClass().add("button");
        runButton.getStyleClass().add("button"); // << استایل دکمه

        // ۳. اتصال اکشن دکمه‌ها به متدهای کنترلر
        shopButton.setOnAction(e -> controller.onShopClicked());
        pauseButton.setOnAction(e -> controller.onPauseClicked());
        menuButton.setOnAction(e -> controller.onMenuClicked());
        runButton.setOnAction(e -> controller.onRunClicked());

        // ۴. چیدمان اجزا
        // HUD در بالا
        HBox hud = new HBox(20, coinsLabel, wireLengthLabel, packetLossLabel);
        hud.setPadding(new Insets(10, 20, 10, 20));
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        this.setTop(hud);

        // دکمه‌ها در سمت راست
        VBox controls = new VBox(15, runButton, shopButton, pauseButton, menuButton);
        controls.setPadding(new Insets(20));
        controls.setAlignment(Pos.TOP_CENTER);
        this.setRight(controls);

        // Canvas در وسط
        this.setCenter(gameCanvas);
    }

    // Getters برای اینکه کنترلر بتواند به این اجزا دسترسی داشته باشد
    public Canvas getGameCanvas() { return gameCanvas; }
    public Label getCoinsLabel() { return coinsLabel; }
    public Label getWireLengthLabel() { return wireLengthLabel; }
    public Label getPacketLossLabel() { return packetLossLabel; }
}