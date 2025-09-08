package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.GamePhase;
import com.BluePrintHell.model.*;
import com.BluePrintHell.model.leveldata.SpawnEventData;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.packets.CirclePacket;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.packets.SquarePacket;
import com.BluePrintHell.model.packets.TrianglePacket;
import com.BluePrintHell.view.GameScreenView;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;
import com.BluePrintHell.util.CollisionManager; // << import جدید

public class GameController {

    private Canvas gameCanvas;
    private Label coinsLabel;
    private Label wireLengthLabel;
    private Label packetLossLabel;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private GameState gameState;
    private GamePhase currentPhase = GamePhase.DESIGN;
    private CollisionManager collisionManager; // << متغیر جدید
    private Button runButton; // << فیلد جدید برای دسترسی به دکمه Run

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
        this.collisionManager = new CollisionManager(50); // 50 is the cell size
        this.runButton = view.getRunButton(); // << گرفتن دکمه از View

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

    private boolean isNetworkReady() {
        if (gameState == null) return false;

        for (NetworkSystem system : gameState.getSystems()) {
            if (!system.isFullyConnected()) {
                System.out.println("Network not ready. System '" + system.getId() + "' has unconnected ports.");
                return false; // اگر حتی یک سیستم هم کامل نباشد، شبکه آماده نیست
            }
        }
        return true; // تمام سیستم‌ها کاملاً متصل هستند
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

    // در فایل GameController.java

    private void onMousePressed(MouseEvent event) {
        if (currentPhase == GamePhase.DESIGN) {
            // --- حالت ۱: اگر در حال کشیدن سیم هستیم (این یعنی کلیک دوم) ---
            if (isDrawingWire) {
                Port endPort = getPortAt(event.getX(), event.getY());

                // در اینجا یک شرط اضافه می‌کنیم تا مطمئن شویم پورت مقصد از نوع ورودی است
                if (endPort != null && endPort.getType() == PortType.INPUT && !endPort.isConnected() &&
                        endPort.getParentSystem() != wireStartPort.getParentSystem()) {

                    // TODO: چک کردن محدودیت طول سیم

                    // اتصال موفق: یک Connection جدید بساز
                    Connection newConnection = new Connection(wireStartPort, endPort);
                    gameState.addConnection(newConnection);
                    wireStartPort.connect(newConnection);
                    endPort.connect(newConnection);
                    System.out.println("Wire connected successfully!");

                } else {
                    System.out.println("Wiring canceled. Invalid endpoint.");
                }

                isDrawingWire = false;
                wireStartPort = null;
            }
            // --- حالت ۲: اگر در حال کشیدن سیم نیستیم (این یعنی کلیک اول) ---
            else {
                Port clickedPort = getPortAt(event.getX(), event.getY());

                if (clickedPort != null) {
                    if (clickedPort.isConnected()) {
                        Connection connectionToRemove = clickedPort.getAttachedConnection();
                        connectionToRemove.getStartPort().disconnect();
                        connectionToRemove.getEndPort().disconnect();
                        gameState.removeConnection(connectionToRemove);
                        System.out.println("Wire removed.");
                    }
                    else if (clickedPort.getType() == PortType.OUTPUT) {
                        isDrawingWire = true;
                        wireStartPort = clickedPort;
                        liveWireEndPoint = new Point2D(event.getX(), event.getY());
                        System.out.println("Started drawing wire from OUTPUT port: " + clickedPort.getId());
                    }
                }
                else {
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
    }

    private void onMouseDragged(MouseEvent event) {
        // فقط اگر یک سیستم قبلاً انتخاب شده باشد، این کد اجرا می‌شود
        if ((selectedSystem != null) && (currentPhase == GamePhase.DESIGN)) {
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

                if (currentPhase == GamePhase.SIMULATION) {
                    updateGame(deltaTime);
                }

                renderGame();
                updateHUD();
            }
        };
    }

    private void updateHUD() {
        if (gameState == null) return;

        coinsLabel.setText("Coins: " + gameState.getPlayerCoins());
        // TODO: Update wireLengthLabel later

        int total = gameState.getTotalPacketsSpawned();
        int lost = gameState.getPacketsLost();
        double lossPercentage = (total == 0) ? 0 : ((double) lost / total) * 100;

        packetLossLabel.setText(String.format("Packet Loss: %.1f%%", lossPercentage));
    }

    private void updateGame(double deltaTime) {
        if (gameState != null) {
            gameState.update(deltaTime);
            System.out.println("DEBUG: Game Time is now: " + gameState.getGameTime());
            handlePacketSpawning();
            collisionManager.checkCollisions(gameState.getPackets());
            checkWinLossConditions();
            gameState.cleanupLists();
        }
    }

    private void checkWinLossConditions() {
        // Condition for Game Over
        int total = gameState.getTotalPacketsSpawned();
        int lost = gameState.getPacketsLost();
        if (total > 0 && ((double) lost / total) > 0.5) {
            gameLoop.stop();
            // استفاده از متد جدید activate با یک لامبدا برای پاس دادن دیتا
            ScreenController.getInstance().activate(Screen.END_SCREEN, (EndScreenController c) -> {
                c.initializeData(gameState, false); // false = isWin
            });
        }

        // Condition for Level Complete
        boolean allSpawnsDone = gameState.getSpawnEvents().isEmpty();
        boolean noPacketsActive = gameState.getPackets().isEmpty();
        boolean allBuffersEmpty = gameState.allSystemBuffersAreEmpty();

        if (allSpawnsDone && noPacketsActive && allBuffersEmpty && gameState.getTotalPacketsSpawned() > 0) {
            gameLoop.stop();
            // استفاده از متد جدید activate با یک لامبدا برای پاس دادن دیتا
            ScreenController.getInstance().activate(Screen.END_SCREEN, (EndScreenController c) -> {
                c.initializeData(gameState, true); // true = isWin
            });
        }
    }
    private Packet createPacketFromType(String packetType, Point2D position) {
        switch (packetType) {
            case "SQUARE_MESSENGER":
                return new SquarePacket(position);
            case "TRIANGLE_MESSENGER":
                return new TrianglePacket(position);
            case "CIRCLE_MESSENGER": // << این case جدید را اضافه کنید
                return new CirclePacket(position);
            default:
                System.err.println("Warning: Unknown packet type '" + packetType + "' in JSON.");
                return null;
        }
    }

    private void handlePacketSpawning() {
        // اگر هیچ رویدادی برای اجرا باقی نمانده، کاری برای انجام نیست.
        if (gameState.getSpawnEvents().isEmpty()) {
            return;
        }

        // به اولین رویداد در لیست نگاه می‌کنیم (فرض بر این است که لیست از قبل مرتب شده).
        SpawnEventData nextEvent = gameState.getSpawnEvents().get(0);

        // چک می‌کنیم آیا زمان تولید این پکت فرا رسیده است یا نه.
        if (gameState.getGameTime() >= nextEvent.getSpawnTime()) {

            // ۱. سیستم مبدا را بر اساس ID که در فایل JSON مشخص شده، پیدا می‌کنیم.
            NetworkSystem sourceSystem = gameState.getSystems().stream()
                    .filter(s -> s.getId().equals(nextEvent.getSourceSystemId()))
                    .findFirst()
                    .orElse(null);

            if (sourceSystem != null) {
                Packet newPacket = createPacketFromType(nextEvent.getPacketType(), new Point2D(0, 0)); // موقعیت اولیه مهم نیست.

                if (newPacket != null) {
                    sourceSystem.receivePacket(newPacket);
                    gameState.incrementTotalPacketsSpawned();
                    System.out.println("Packet created...");
                }
            } else {
                System.err.println("Error: Could not find source system with ID: " + nextEvent.getSourceSystemId());
            }

            // ۴. رویداد انجام شده را از لیست حذف می‌کنیم تا دوباره اجرا نشود.
            gameState.getSpawnEvents().remove(0);
        }
    }
    private void renderGame() {
        if (currentPhase == GamePhase.DESIGN) {
            runButton.setDisable(!isNetworkReady());
        }
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        if (gameState == null) return;
        System.out.println("DEBUG: Packets to render: " + gameState.getPackets().size());

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

        for (Packet packet : gameState.getPackets()) {
            drawPacket(packet);
        }

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

        double indicatorSize = 10;
        double indicatorX = x + SYSTEM_WIDTH - indicatorSize - 5;
        double indicatorY = y + 5;

        if (system.isFullyConnected()) {
            // چراغ سبز روشن برای وضعیت آماده
            gc.setFill(Color.web("#39FF14")); // سبز نئونی
            gc.setEffect(new DropShadow(15, Color.web("#39FF14")));
        } else {
            // چراغ قرمز خاموش برای وضعیت غیرآماده
            gc.setFill(Color.web("#4d0000")); // قرمز تیره
            gc.setEffect(null);
        }
        gc.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
        gc.setEffect(null);
    }

    private void drawPacket(Packet packet) {
        if (packet == null) return;

        Point2D pos = packet.getPosition();
        double size = 10; // اندازه پکت روی صفحه

        // بر اساس نوع کلاس پکت، شکل متفاوتی رسم می‌کنیم
        if (packet instanceof SquarePacket) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        } else if (packet instanceof TrianglePacket) {
            gc.setFill(Color.YELLOW);
            double[] xPoints = {pos.getX(), pos.getX() + size, pos.getX() + size / 2};
            double[] yPoints = {pos.getY() + size, pos.getY() + size, pos.getY()};
            gc.fillPolygon(xPoints, yPoints, 3);
        }  else if (packet instanceof CirclePacket) { // << این بخش جدید را اضافه کنید
            gc.setFill(Color.ORANGE); // یک رنگ جدید برای پکت دایره
            gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        }
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
            case CIRCLE:
                gc.fillOval(x, y, PORT_SIZE, PORT_SIZE);
                break;
        }

        gc.setEffect(null);
    }

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
                return Color.web("#00f0ff");
            case TRIANGLE:
                return Color.web("#ff00ff");
            case CIRCLE:
                return Color.ORANGE;
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

    @FXML
    public void onRunClicked() {
        if (currentPhase == GamePhase.DESIGN && isNetworkReady()) {
            System.out.println("--- SIMULATION STARTED ---");
            if (gameState != null) {
                gameState.resetGameTime();
            }
            currentPhase = GamePhase.SIMULATION;
            runButton.setDisable(true); // دکمه Run را غیرفعال کن
        } else {
            System.out.println("Cannot start simulation. All system ports must be connected.");
        }
    }
}