package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.view.GameScreenView;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;

public class GameController {

    private Canvas gameCanvas;
    private Label coinsLabel;
    private Label wireLengthLabel;
    private Label packetLossLabel;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private GameState gameState;

    private NetworkSystem selectedSystem = null; // سیستمی که برای جابجایی انتخاب شده
    private double offsetX; // فاصله افقی کلیک ماوس تا گوشه سیستم
    private double offsetY;

    private boolean isDrawingWire = false; // آیا در حال کشیدن سیم هستیم؟
    private Port wireStartPort = null; // پورتی که سیم‌کشی از آن شروع شده
    private Point2D liveWireEndPoint = null;

    // ثوابت برای ابعاد گرافیکی
    private static final double SYSTEM_WIDTH = 120;
    private static final double SYSTEM_HEIGHT = 80;
    private static final double PORT_SIZE = 12;

    // این متد جایگزین initialize می‌شود
    public void initializeGame(GameScreenView view) {
        // ۱. گرفتن اجزای گرافیکی از View
        this.gameCanvas = view.getGameCanvas();
        this.coinsLabel = view.getCoinsLabel();
        this.wireLengthLabel = view.getWireLengthLabel();
        this.packetLossLabel = view.getPacketLossLabel();

        // ۲. گرفتن وضعیت بازی از GameManager
        this.gameState = GameManager.getInstance().getCurrentGameState();
        if (this.gameState == null) {
            System.err.println("FATAL: GameState is null.");
            return;
        }

        // ۳. راه‌اندازی بقیه موارد
        this.gc = gameCanvas.getGraphicsContext2D();

        gameCanvas.setOnMousePressed(this::onMousePressed);
        gameCanvas.setOnMouseDragged(this::onMouseDragged);
        gameCanvas.setOnMouseReleased(this::onMouseReleased);
        gameCanvas.setOnMouseMoved(this::onMouseMoved); // <<< این خط جدید است

        calculateInitialPositions();
        setupGameLoop();
        gameLoop.start();
    }

    private void calculateInitialPositions() {
        System.out.println("DEBUG: Calculating initial port positions..."); // این خط را اضافه کنید
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
                System.out.println(" -> Set position for INPUT port '" + port.getId() + "'"); // این خط
            }

            // محاسبه موقعیت پورت‌های خروجی (سمت راست)
            int outputPortCount = system.getOutputPorts().size();
            for (int i = 0; i < outputPortCount; i++) {
                Port port = system.getOutputPorts().get(i);
                double portX = sysX + SYSTEM_WIDTH - (PORT_SIZE / 2);
                double portY = sysY + (SYSTEM_HEIGHT * (i + 1) / (outputPortCount + 1)) - (PORT_SIZE / 2);
                port.setPosition(new Point2D(portX, portY));
                System.out.println(" -> Set position for OUTPUT port '" + port.getId() + "'"); // این خط
            }
        }
    }

    private void updatePortPositions(NetworkSystem system) {
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

    private void onMousePressed(MouseEvent event) {
        // اگر در حال کشیدن سیم هستیم (این کلیک دوم است)
        if (isDrawingWire) {
            Port endPort = getPortAt(event.getX(), event.getY());

            // بررسی شرایط برای اتصال موفق
            if (endPort != null && !endPort.isConnected() &&
                    endPort.getParentSystem() != wireStartPort.getParentSystem() &&
                    endPort.getType() != wireStartPort.getType()) {

                // TODO: چک کردن محدودیت طول سیم

                // اتصال موفق: یک Connection جدید بساز
                Connection newConnection = new Connection(wireStartPort, endPort);
                gameState.addConnection(newConnection);
                wireStartPort.connect(newConnection);
                endPort.connect(newConnection);

                System.out.println("Wire connected successfully!");

            } else {
                // اتصال ناموفق یا لغو
                System.out.println("Wiring canceled.");
            }
            // در هر صورت، عملیات سیم‌کشی تمام می‌شود
            isDrawingWire = false;
            wireStartPort = null;

        } else { // اگر در حال کشیدن سیم نیستیم (این کلیک اول است)
            Port clickedPort = getPortAt(event.getX(), event.getY());

            // اگر روی یک پورت خالی کلیک شده
            if (clickedPort != null && !clickedPort.isConnected()) {
                isDrawingWire = true;
                wireStartPort = clickedPort;
                liveWireEndPoint = new Point2D(event.getX(), event.getY());

            } else { // اگر روی پورت کلیک نشده، منطق جابجایی سیستم را اجرا کن
                // (این کد همان کد قبلی برای درگ اند دراپ است)
                for (int i = gameState.getSystems().size() - 1; i >= 0; i--) {
                    NetworkSystem system = gameState.getSystems().get(i);
                    double sysX = system.getPosition().getX();
                    double sysY = system.getPosition().getY();
                    if (event.getX() >= sysX && event.getX() <= sysX + SYSTEM_WIDTH &&
                            event.getY() >= sysY && event.getY() <= sysY + SYSTEM_HEIGHT) {
                        selectedSystem = system;
                        offsetX = event.getX() - sysX;
                        offsetY = event.getY() - sysY;
                        return;
                    }
                }
            }
        }
    }

    private void onMouseDragged(MouseEvent event) {
        // فقط اگر یک سیستم قبلاً انتخاب شده باشد، این کد اجرا می‌شود
        if (selectedSystem != null) {
            // موقعیت جدید سیستم را بر اساس موقعیت ماوس و آفست محاسبه می‌کنیم
            double newX = event.getX() - offsetX;
            double newY = event.getY() - offsetY;
            selectedSystem.setPosition(new Point2D(newX, newY));

            // --- نکته بسیار مهم: آپدیت کردن موقعیت پورت‌ها ---
            updatePortPositions(selectedSystem);
        }
    }

    private void onMouseReleased(MouseEvent event) {
        // با رها شدن دکمه ماوس، دیگر هیچ سیستمی انتخاب شده نیست
        selectedSystem = null;
    }

    private void onMouseMoved(MouseEvent event) {
        if (isDrawingWire) {
            liveWireEndPoint = new Point2D(event.getX(), event.getY());
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

    // در فایل GameController.java

    // در فایل GameController.java

    private void renderGame() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        if (gameState == null) return;

        // ==========================================================
        // === بخش جدید: نقاشی سیم‌های دائمی و پیش‌نمایش ===
        // ==========================================================

        // ۱. نقاشی سیم‌های دائمی (اتصالات برقرار شده)
        gc.setStroke(Color.web("#7a7f8a", 0.7)); // رنگ خاکستری با کمی شفافیت
        gc.setLineWidth(4);
        for (Connection connection : gameState.getConnections()) {
            Point2D start = connection.getStartPort().getPosition();
            Point2D end = connection.getEndPort().getPosition();
            // مرکز پورت‌ها را به هم وصل می‌کنیم
            gc.strokeLine(start.getX() + PORT_SIZE / 2, start.getY() + PORT_SIZE / 2,
                    end.getX() + PORT_SIZE / 2, end.getY() + PORT_SIZE / 2);
        }

        // ۲. نقاشی سیم پیش‌نمایش (اگر در حال کشیدن سیم باشیم)
        if (isDrawingWire && wireStartPort != null && liveWireEndPoint != null) {
            gc.setStroke(Color.web("#00f0ff", 0.9)); // رنگ آبی نئونی
            gc.setLineWidth(3);
            Point2D start = wireStartPort.getPosition();
            // TODO: منطق محدودیت طول سیم را اینجا پیاده کنید
            gc.strokeLine(start.getX() + PORT_SIZE / 2, start.getY() + PORT_SIZE / 2,
                    liveWireEndPoint.getX(), liveWireEndPoint.getY());
        }

        // ==========================================================
        // === پایان بخش جدید ===
        // ==========================================================

        // ۱. ابتدا تمام بدنه‌های سیستم‌ها را نقاشی کن
        for (NetworkSystem system : gameState.getSystems()) {
            drawSystem(system);
        }

        // ۲. سپس تمام پورت‌ها را روی سیستم‌ها نقاشی کن
        for (NetworkSystem system : gameState.getSystems()) {
            for (Port port : system.getInputPorts()) {
                drawPort(port);
            }
            for (Port port : system.getOutputPorts()) {
                drawPort(port);
            }
        }

        // TODO: نقاشی پکت‌ها (Packets)
    }
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

    // در فایل GameController.java

    private Port getPortAt(double x, double y) {
        for (NetworkSystem system : gameState.getSystems()) {
            // چک کردن تمام پورت‌ها (ورودی و خروجی)
            for (Port port : system.getInputPorts()) {
                // ۱. محاسبه مرکز پورت
                Point2D portCenter = new Point2D(
                        port.getPosition().getX() + PORT_SIZE / 2,
                        port.getPosition().getY() + PORT_SIZE / 2
                );
                // ۲. بررسی فاصله از مرکز
                if (portCenter.distance(x, y) < PORT_SIZE / 2) {
                    return port;
                }
            }
            for (Port port : system.getOutputPorts()) {
                // ۱. محاسبه مرکز پورت
                Point2D portCenter = new Point2D(
                        port.getPosition().getX() + PORT_SIZE / 2,
                        port.getPosition().getY() + PORT_SIZE / 2
                );
                // ۲. بررسی فاصله از مرکز
                if (portCenter.distance(x, y) < PORT_SIZE / 2) {
                    return port;
                }
            }
        }
        return null;
    }
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

    public void onShopClicked() {
        System.out.println("Opening shop...");
        ScreenController.getInstance().activate(Screen.SHOP);
    }

    public void onPauseClicked() {
        System.out.println("Game paused/resumed!");
        // TODO: منطق توقف و ادامه حلقه بازی را اینجا پیاده‌سازی کنید
        // gameLoop.stop() or gameLoop.start()
    }

    public void onMenuClicked() {
        System.out.println("Returning to Main Menu...");
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }
}