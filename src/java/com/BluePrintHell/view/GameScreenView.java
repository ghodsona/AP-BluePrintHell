package com.BluePrintHell.view;

import com.BluePrintHell.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameScreenView extends BorderPane {

    private Canvas gameCanvas;
    private Label coinsLabel;
    private Label wireLengthLabel;
    private Label packetLossLabel;
    private Button runButton;

    public GameScreenView(GameController controller) {
        gameCanvas = new Canvas(1200, 800);
        coinsLabel = new Label("Coins: 0");
        wireLengthLabel = new Label("Wire Length: 1000");
        packetLossLabel = new Label("Packet Loss: 0%");

        runButton = new Button("Run");
        Button shopButton = new Button("Shop");
        Button pauseButton = new Button("Pause");
        Button menuButton = new Button("Menu");

        this.getStyleClass().add("root-pane");
        coinsLabel.getStyleClass().add("hud-label");
        wireLengthLabel.getStyleClass().add("hud-label");
        packetLossLabel.getStyleClass().add("hud-label");
        shopButton.getStyleClass().add("button");
        pauseButton.getStyleClass().add("button");
        menuButton.getStyleClass().add("button");
        runButton.getStyleClass().add("button");

        shopButton.setOnAction(e -> controller.onShopClicked());
        pauseButton.setOnAction(e -> controller.onPauseClicked());
        menuButton.setOnAction(e -> controller.onMenuClicked());
        runButton.setOnAction(e -> controller.onRunClicked());

        HBox hud = new HBox(20, coinsLabel, wireLengthLabel, packetLossLabel);
        hud.setPadding(new Insets(10, 20, 10, 20));
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        this.setTop(hud);

        VBox controls = new VBox(15, runButton, shopButton, pauseButton, menuButton);
        controls.setPadding(new Insets(20));
        controls.setAlignment(Pos.TOP_CENTER);
        this.setRight(controls);

        // ✅✅✅ تغییر اصلی اینجاست ✅✅✅
        // یک AnchorPane می‌سازیم تا Canvas را در بر بگیرد
        AnchorPane canvasContainer = new AnchorPane();
        canvasContainer.getChildren().add(gameCanvas);

        // حالا AnchorPane را در مرکز BorderPane قرار می‌دهیم
        this.setCenter(canvasContainer);
    }

    public Canvas getGameCanvas() { return gameCanvas; }
    public Label getCoinsLabel() { return coinsLabel; }
    public Label getWireLengthLabel() { return wireLengthLabel; }
    public Label getPacketLossLabel() { return packetLossLabel; }
    public Button getRunButton() { return runButton; }
}