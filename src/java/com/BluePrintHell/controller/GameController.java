package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;

public class GameController implements Initializable {

    // --- FXML Components ---
    @FXML
    private AnchorPane canvasContainer;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label coinsLabel;
    @FXML
    private Label wireLengthLabel;
    @FXML
    private Label packetLossLabel;

    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private GameState gameState;

    // ثوابت برای ابعاد گرافیکی
    private static final double SYSTEM_WIDTH = 120;
    private static final double SYSTEM_HEIGHT = 80;
    private static final double PORT_SIZE = 12;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameState = GameManager.getInstance().getCurrentGameState();
        if (this.gameState == null) {
            System.err.println("FATAL: GameState is null. Could not start game.");
            return;
        }
        System.out.println("3. GameController initialized with GameState containing " + this.gameState.getSystems().size() + " systems.");

        // GraphicsContext را برای نقاشی روی Canvas آماده می‌کنیم
        gc = gameCanvas.getGraphicsContext2D();

        gameCanvas.widthProperty().bind(canvasContainer.widthProperty());
        gameCanvas.heightProperty().bind(canvasContainer.heightProperty());

        calculateInitialPositions();
        setupGameLoop();
        gameLoop.start();
    }

    private void calculateInitialPositions() {
        for (NetworkSystem system : gameState.getSystems()) {
            double sysX = system.getPosition().getX();
            double sysY = system.getPosition().getY();

            // محاسبه موقعیت پورت‌های ورودی (سمت چپ)
            int inputPortCount = system.getInputPorts().size();
            for (int i = 0; i < inputPortCount; i++) {
                Port port = system.getInputPorts().get(i);
                double portX = sysX - (PORT_SIZE / 2);
                double portY = sysY + (SYSTEM_HEIGHT * (i + 1) / (inputPortCount + 1)) - (PORT_SIZE / 2);
                port.setPosition(new Point2D(portX, portY));
            }

            // محاسبه موقعیت پورت‌های خروجی (سمت راست)
            int outputPortCount = system.getOutputPorts().size();
            for (int i = 0; i < outputPortCount; i++) {
                Port port = system.getOutputPorts().get(i);
                double portX = sysX + SYSTEM_WIDTH - (PORT_SIZE / 2);
                double portY = sysY + (SYSTEM_HEIGHT * (i + 1) / (outputPortCount + 1)) - (PORT_SIZE / 2);
                port.setPosition(new Point2D(portX, portY));
            }
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                // ۱. آپدیت کردن منطق بازی (حرکت پکت‌ها، برخوردها و ...)
                updateGame(deltaTime);

                // ۲. نقاشی کردن وضعیت جدید روی صفحه
                renderGame();
            }
        };
    }

    private void updateGame(double deltaTime) {
        // TODO: تمام منطق بازی اینجا پیاده‌سازی می‌شود
        // برای مثال: packet.move(deltaTime);
        if (gameState != null) {
            gameState.update(deltaTime);
        }
    }

    // ... در داخل کلاس GameController.java

    private void renderGame() {
        System.out.println("4. Rendering frame...");
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        if (gameState == null) return;

        // TODO: نقاشی سیم‌ها (Connections) در قدم بعدی اینجا خواهد بود

        // نقاشی تمام سیستم‌ها و پورت‌هایشان
        for (NetworkSystem system : gameState.getSystems()) {
            drawSystem(system);
            for (Port port : system.getInputPorts()) {
                drawPort(port);
            }
            for (Port port : system.getOutputPorts()) {
                drawPort(port);
            }
        }

        // TODO: نقاشی پکت‌ها (Packets) در قدم‌های بعدی اینجا خواهد بود
    }

    /**
     * یک سیستم شبکه را با استایل مورد نظر نقاشی می‌کند.
     */
    private void drawSystem(NetworkSystem system) {
        double x = system.getPosition().getX();
        double y = system.getPosition().getY();

        // بدنه اصلی سیستم با گرادینت
        gc.setFill(Color.web("#4a4f5a"));
        gc.fillRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);

        // حاشیه برای عمق بخشیدن
        gc.setStroke(Color.web("#7a7f8a"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);

        // یک خط تزئینی در بالا
        gc.setStroke(Color.web("#00f0ff", 0.5)); // آبی نئونی با شفافیت
        gc.setLineWidth(3);
        gc.strokeLine(x + 5, y + 5, x + SYSTEM_WIDTH - 5, y + 5);
    }

    /**
     * یک پورت را با شکل، رنگ و افکت درخشش نقاشی می‌کند.
     */
    private void drawPort(Port port) {
        if (port.getPosition() == null) return;

        double x = port.getPosition().getX();
        double y = port.getPosition().getY();
        Color portColor = getColorForShape(port.getShape());

        // افکت درخشش (Glow)
        gc.setEffect(new javafx.scene.effect.DropShadow(10, portColor));

        gc.setFill(portColor);

        // نقاشی شکل پورت بر اساس نوع آن
        switch (port.getShape()) {
            case SQUARE:
                gc.fillRect(x, y, PORT_SIZE, PORT_SIZE);
                break;
            case TRIANGLE:
                double[] xPoints = {x, x + PORT_SIZE, x + (PORT_SIZE / 2)};
                double[] yPoints = {y + PORT_SIZE, y + PORT_SIZE, y};
                gc.fillPolygon(xPoints, yPoints, 3);
                break;
            // ... بقیه شکل‌ها
        }

        // غیرفعال کردن افکت برای عناصر بعدی
        gc.setEffect(null);
    }

    /**
     * بر اساس شکل پورت، رنگ مناسب را برمی‌گرداند.
     */
    private Color getColorForShape(PortShape shape) {
        switch (shape) {
            case SQUARE:
                return Color.web("#00f0ff"); // آبی نئونی
            case TRIANGLE:
                return Color.web("#ff00ff"); // صورتی نئونی
            default:
                return Color.WHITE;
        }
    }
    // --- FXML Action Methods ---
    @FXML
    private void onShopClicked() {
        System.out.println("Opening shop...");
        ScreenController.getInstance().activate(Screen.SHOP);
    }

    @FXML
    private void onPauseClicked() {
        System.out.println("Game paused/resumed!");
        // TODO: منطق توقف و ادامه حلقه بازی را اینجا پیاده‌سازی کنید
        // gameLoop.stop() or gameLoop.start()
    }

    @FXML
    private void onMenuClicked() {
        System.out.println("Returning to Main Menu...");
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }
}