package com.BluePrintHell.controller;

import com.BluePrintHell.model.network.*;
import com.BluePrintHell.model.packets.*;
import com.BluePrintHell.util.SaveManager;
import com.BluePrintHell.GameManager;
import com.BluePrintHell.GamePhase;
import com.BluePrintHell.model.*;
import com.BluePrintHell.model.leveldata.SpawnEventData;
import com.BluePrintHell.view.GameScreenView;
import com.BluePrintHell.util.CollisionManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class GameController {

    private Canvas gameCanvas;
    private AnchorPane canvasContainer;
    private Label coinsLabel;
    private Label wireLengthLabel;
    private Label packetLossLabel;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private GameState gameState;
    private GamePhase currentPhase = GamePhase.DESIGN;
    private CollisionManager collisionManager;
    private Button runButton;
    private long lastSaveTime = 0;
    private static final long SAVE_INTERVAL_NANO = 5_000_000_000L;

    private Image protectedPacketImage;
    private Image confidentialPacketImage;
    private Image protectedConfidentialPacketImage;

    private NetworkSystem selectedSystem = null;
    private double offsetX, offsetY;

    private boolean isDrawingWire = false;
    private Port wireStartPort = null;
    private Point2D liveWireEndPoint = null;

    private Point2D selectedBendPoint = null;
    private Connection selectedConnectionForBending = null;
    private int selectedBendPointIndex = -1;

    private static final double SYSTEM_WIDTH = 120;
    private static final double SYSTEM_HEIGHT = 80;
    private static final double PORT_SIZE = 12;
    private static final double BEND_POINT_SIZE = 8;
    private static final int MAX_BEND_POINTS = 3;

    public void initializeGame(GameScreenView view) {
        this.gameCanvas = view.getGameCanvas();
        this.canvasContainer = (AnchorPane) view.getCenter();
        this.coinsLabel = view.getCoinsLabel();
        this.wireLengthLabel = view.getWireLengthLabel();
        this.packetLossLabel = view.getPacketLossLabel();
        this.runButton = view.getRunButton();
        this.collisionManager = new CollisionManager(50);
        this.gameState = GameManager.getInstance().getCurrentGameState();
        if (this.gameState == null) {
            System.err.println("FATAL: GameState is null.");
            return;
        }

        try {
            String imagePath = "/com/BluePrintHell/view/images/PROTECTED.png";
            protectedPacketImage = new Image(getClass().getResourceAsStream(imagePath));
            System.out.println("Protected packet image loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load protected_packet.png. Using fallback color.");
            protectedPacketImage = null;
        }
        try {
            String confidentialImagePath = "/com/BluePrintHell/view/images/CONFIDENTIAL.png";
            confidentialPacketImage = new Image(getClass().getResourceAsStream(confidentialImagePath));
            System.out.println("Confidential packet image loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load CONFIDENTIAL.png. Using fallback color.");
            confidentialPacketImage = null;
        }

        try {
            String protectedConfidentialImagePath = "/com/BluePrintHell/view/images/PROTECTED_CONFIDENTIAL.png";
            protectedConfidentialPacketImage = new Image(getClass().getResourceAsStream(protectedConfidentialImagePath));
            System.out.println("Protected Confidential packet image loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load PROTECTED_CONFIDENTIAL.png. Will use general protected image or fallback.");
            protectedConfidentialPacketImage = null;
        }

        this.gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setOnMousePressed(this::onMousePressed);
        gameCanvas.setOnMouseDragged(this::onMouseDragged);
        gameCanvas.setOnMouseReleased(this::onMouseReleased);
        gameCanvas.setOnMouseMoved(this::onMouseMoved);
        calculateInitialPositions();
        setupGameLoop();

        lastSaveTime = System.nanoTime();

        gameLoop.start();
    }

    private void onMousePressed(MouseEvent event) {
        if (currentPhase != GamePhase.DESIGN) return;
        Point2D clickPoint = new Point2D(event.getX(), event.getY());

        for (Connection conn : gameState.getConnections()) {
            for (int i = 0; i < conn.getBendPoints().size(); i++) {
                if (conn.getBendPoints().get(i).distance(clickPoint) < BEND_POINT_SIZE) {
                    selectedBendPoint = conn.getBendPoints().get(i);
                    selectedConnectionForBending = conn;
                    selectedBendPointIndex = i;
                    return;
                }
            }
        }
        if (event.getClickCount() == 2 && !isDrawingWire) {
            handleDoubleClick(clickPoint);
            return;
        }
        Port clickedPort = getPortAt(clickPoint.getX(), clickPoint.getY());
        if (clickedPort != null) {
            handlePortClick(clickedPort);
        } else {
            handleSystemDragStart(event);
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (currentPhase != GamePhase.DESIGN) return;
        Point2D currentPoint = new Point2D(event.getX(), event.getY());

        if (selectedBendPoint != null) {
            Point2D originalPosition = selectedBendPoint;
            selectedConnectionForBending.getBendPoints().set(selectedBendPointIndex, currentPoint);
            if (!isConnectionValid(selectedConnectionForBending)) {
                selectedConnectionForBending.getBendPoints().set(selectedBendPointIndex, originalPosition);
            } else {
                selectedBendPoint = currentPoint;
            }
        } else if (selectedSystem != null) {
            double newX = event.getX() - offsetX;
            double newY = event.getY() - offsetY;
            selectedSystem.setPosition(new Point2D(newX, newY));
            updatePortPositions(selectedSystem);
        } else if (isDrawingWire) {
            liveWireEndPoint = currentPoint;
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (isDrawingWire) {
            Port endPort = getPortAt(event.getX(), event.getY());
            if (endPort != null && endPort.getType() == PortType.INPUT && !endPort.isConnected() && endPort.getParentSystem() != wireStartPort.getParentSystem()) {
                Connection newConnection = new Connection(wireStartPort, endPort);
                if (isConnectionValid(newConnection)) {
                    gameState.addConnection(newConnection);
                    wireStartPort.connect(newConnection);
                    endPort.connect(newConnection);
                }
            }
            isDrawingWire = false;
            wireStartPort = null;
        }
        selectedSystem = null;
        selectedBendPoint = null;
        selectedConnectionForBending = null;
        selectedBendPointIndex = -1;
    }

    public void renderGame() {
        if (gameState == null) return;
        if (currentPhase == GamePhase.DESIGN) {
            runButton.setDisable(!isNetworkReady());
        }
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        for (Connection connection : gameState.getConnections()) {
            drawConnection(connection);
        }
        if (isDrawingWire && wireStartPort != null && liveWireEndPoint != null) {
            drawPreviewWire();
        }
        for (NetworkSystem system : gameState.getSystems()) {
            drawSystem(system);
        }
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

    private boolean isSegmentIntersectingAnySystem(Point2D p1, Point2D p2, Connection connection) {
        for (NetworkSystem system : gameState.getSystems()) {
            if (system == connection.getStartPort().getParentSystem() || system == connection.getEndPort().getParentSystem()) {
                continue;
            }
            double sysX = system.getPosition().getX();
            double sysY = system.getPosition().getY();
            if (Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), sysX, sysY, sysX + SYSTEM_WIDTH, sysY) ||
                    Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), sysX + SYSTEM_WIDTH, sysY, sysX + SYSTEM_WIDTH, sysY + SYSTEM_HEIGHT) ||
                    Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), sysX + SYSTEM_WIDTH, sysY + SYSTEM_HEIGHT, sysX, sysY + SYSTEM_HEIGHT) ||
                    Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), sysX, sysY + SYSTEM_HEIGHT, sysX, sysY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        List<Point2D> pathPoints = conn.getPathPoints();
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            if (isSegmentIntersectingAnySystem(pathPoints.get(i), pathPoints.get(i + 1), conn)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNetworkReady() {
        if (gameState == null) return false;

        if (getTotalWireLength() > gameState.getPlayerWireLength()) {
            return false;
        }

        for (NetworkSystem system : gameState.getSystems()) {
            if (!system.isFullyConnected()) {
                return false;
            }
        }

        for (Connection conn : gameState.getConnections()) {
            if (!isConnectionValid(conn)) {
                return false;
            }
        }

        return true;
    }

    private void drawConnection(Connection connection) {
        if (isConnectionValid(connection)) {
            gc.setStroke(Color.web("#7a7f8a", 0.7));
        } else {
            gc.setStroke(Color.web("#ff3b3b", 0.9));
        }
        gc.setLineWidth(4);

        List<Point2D> smoothPath = connection.getSmoothPath(20);
        if (smoothPath.size() < 2) return;

        gc.beginPath();
        gc.moveTo(smoothPath.get(0).getX(), smoothPath.get(0).getY());
        for (int i = 1; i < smoothPath.size(); i++) {
            gc.lineTo(smoothPath.get(i).getX(), smoothPath.get(i).getY());
        }
        gc.stroke();
        gc.closePath();

        gc.setFill(Color.web("#00f0ff"));
        for (Point2D bendPoint : connection.getBendPoints()) {
            gc.fillOval(bendPoint.getX() - BEND_POINT_SIZE / 2, bendPoint.getY() - BEND_POINT_SIZE / 2, BEND_POINT_SIZE, BEND_POINT_SIZE);
        }
    }

    private void drawPreviewWire() {
        Point2D start = wireStartPort.getCenterPosition();
        gc.setStroke(Color.web("#00f0ff", 0.9));
        gc.setLineWidth(3);
        gc.strokeLine(start.getX(), start.getY(), liveWireEndPoint.getX(), liveWireEndPoint.getY());
    }

    private double getTotalWireLength() {
        double total = 0;
        for (Connection conn : gameState.getConnections()) {
            total += conn.calculateLength();
        }
        return total;
    }

    private void updateHUD() {
        if (gameState == null) return;
        coinsLabel.setText("Coins: " + gameState.getPlayerCoins());

        double usedWire = getTotalWireLength();
        double maxWire = gameState.getPlayerWireLength();
        double remainingWire = maxWire - usedWire;

        wireLengthLabel.setText(String.format("Remaining Wire: %.0f / %.0f", remainingWire, maxWire));

        if (remainingWire < 0) {
            wireLengthLabel.getStyleClass().removeAll("hud-label");
            wireLengthLabel.getStyleClass().add("hud-label-error");
        } else {
            wireLengthLabel.getStyleClass().removeAll("hud-label-error");
            wireLengthLabel.getStyleClass().add("hud-label");
        }

        int total = gameState.getTotalPacketsSpawned();
        int lost = gameState.getPacketsLost();
        double lossPercentage = (total == 0) ? 0 : ((double) lost / total) * 100;
        packetLossLabel.setText(String.format("Packet Loss: %.1f%%", lossPercentage));
    }

    private void handleDoubleClick(Point2D clickPoint) {
        for (Connection conn : gameState.getConnections()) {
            if (conn.getDistanceFromPoint(clickPoint) < 5) {
                if (conn.getBendPoints().size() < MAX_BEND_POINTS) {
                    if (gameState.getPlayerCoins() >= 1) {
                        gameState.addCoins(-1);
                        conn.getBendPoints().add(clickPoint);
                    }
                }
                return;
            }
        }
    }

    private void handlePortClick(Port clickedPort) {
        if (clickedPort.isConnected()) {
            Connection connectionToRemove = clickedPort.getAttachedConnection();
            connectionToRemove.getStartPort().disconnect();
            connectionToRemove.getEndPort().disconnect();
            gameState.removeConnection(connectionToRemove);
        } else if (clickedPort.getType() == PortType.OUTPUT) {
            isDrawingWire = true;
            wireStartPort = clickedPort;
            liveWireEndPoint = clickedPort.getCenterPosition();
        }
    }

    private void handleSystemDragStart(MouseEvent event) {
        for (int i = gameState.getSystems().size() - 1; i >= 0; i--) {
            NetworkSystem system = gameState.getSystems().get(i);
            Point2D sysPos = system.getPosition();
            if (event.getX() >= sysPos.getX() && event.getX() <= sysPos.getX() + SYSTEM_WIDTH &&
                    event.getY() >= sysPos.getY() && event.getY() <= sysPos.getY() + SYSTEM_HEIGHT) {
                selectedSystem = system;
                offsetX = event.getX() - sysPos.getX();
                offsetY = event.getY() - sysPos.getY();
                return;
            }
        }
    }

    private void onMouseMoved(MouseEvent event) {
        if (isDrawingWire) {
            liveWireEndPoint = new Point2D(event.getX(), event.getY());
        }
    }

    private void calculateInitialPositions() {
        for (NetworkSystem system : gameState.getSystems()) {
            updatePortPositions(system);
        }
    }

    private void updatePortPositions(NetworkSystem system) {
        double sysX = system.getPosition().getX();
        double sysY = system.getPosition().getY();
        int inputPortCount = system.getInputPorts().size();
        for (int i = 0; i < inputPortCount; i++) {
            Port port = system.getInputPorts().get(i);
            double portX = sysX - (PORT_SIZE / 2);
            double portY = sysY + (SYSTEM_HEIGHT * (i + 1) / (inputPortCount + 1)) - (PORT_SIZE / 2);
            port.setPosition(new Point2D(portX, portY));
        }
        int outputPortCount = system.getOutputPorts().size();
        for (int i = 0; i < outputPortCount; i++) {
            Port port = system.getOutputPorts().get(i);
            double portX = sysX + SYSTEM_WIDTH - (PORT_SIZE / 2);
            double portY = sysY + (SYSTEM_HEIGHT * (i + 1) / (outputPortCount + 1)) - (PORT_SIZE / 2);
            port.setPosition(new Point2D(portX, portY));
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

                if (now - lastSaveTime > SAVE_INTERVAL_NANO) {
                    SaveManager.saveGame(gameState);
                    lastSaveTime = now;
                    System.out.println("Game autosaved.");
                }

                renderGame();
                updateHUD();
            }
        };
    }

    private void updateGame(double deltaTime) {
        if (gameState != null) {
            gameState.update(deltaTime);
            handlePacketSpawning();
            collisionManager.checkCollisions(gameState.getPackets());
            checkWinLossConditions();
            gameState.cleanupLists();
        }
    }

    private void checkWinLossConditions() {
        int total = gameState.getTotalPacketsSpawned();
        int lost = gameState.getPacketsLost();
        if (total > 0 && ((double) lost / total) > 0.5) {
            gameLoop.stop();
            ScreenController.getInstance().activate(Screen.END_SCREEN, (EndScreenController c) -> c.initializeData(gameState, false));
        }
        boolean allSpawnsDone = gameState.getSpawnEvents().isEmpty();
        boolean noPacketsActive = gameState.getPackets().isEmpty();
        boolean allBuffersEmpty = gameState.allSystemBuffersAreEmpty();
        if (allSpawnsDone && noPacketsActive && allBuffersEmpty && gameState.getTotalPacketsSpawned() > 0) {
            gameLoop.stop();
            ScreenController.getInstance().activate(Screen.END_SCREEN, (EndScreenController c) -> c.initializeData(gameState, true));
        }
    }

    private Packet createPacketFromType(String packetType, Point2D position) {
        switch (packetType) {
            case "SQUARE_MESSENGER": return new SquarePacket(position);
            case "CONFIDENTIAL_MESSENGER": return new ConfidentialPacket(position);
            case "PROTECTED_CONFIDENTIAL": return new ProtectedConfidentialPacket(position);
            case "TRIANGLE_MESSENGER": return new TrianglePacket(position);
            case "CIRCLE_MESSENGER": return new CirclePacket(position);
            case "LargePacketTypeA": return new LargePacketTypeA(position);
            case "LargePacketTypeB": return new LargePacketTypeB(position);
            default:
                System.err.println("Unknown packet type in level data: " + packetType);
                return null;
        }
    }

    private void handlePacketSpawning() {
        if (gameState.getSpawnEvents().isEmpty()) return;
        List<SpawnEventData> eventsToRemove = new ArrayList<>();
        for (SpawnEventData nextEvent : gameState.getSpawnEvents()) {
            if (gameState.getGameTime() >= nextEvent.getSpawnTime()) {
                NetworkSystem sourceSystem = gameState.getSystems().stream()
                        .filter(s -> s.getId().equals(nextEvent.getSourceSystemId())).findFirst().orElse(null);
                if (sourceSystem != null) {
                    Packet newPacket = createPacketFromType(nextEvent.getPacketType(), new Point2D(0, 0));
                    if (newPacket != null) {
                        System.out.println("DEBUG: Spawning packet " + newPacket.hashCode() + " of type " + nextEvent.getPacketType() + " at system " + sourceSystem.getId());
                        sourceSystem.receivePacket(newPacket);
                        gameState.incrementTotalPacketsSpawned();
                    }
                }
                eventsToRemove.add(nextEvent);
            }
        }
        gameState.getSpawnEvents().removeAll(eventsToRemove);
    }

    private void drawSystem(NetworkSystem system) {
        double x = system.getPosition().getX();
        double y = system.getPosition().getY();

        Color systemColor = Color.web("#4a4f5a");
        String systemLabel = "";

        // تعیین رنگ و لیبل بر اساس نوع سیستم
        if (system instanceof DistributeSystem) {
            systemColor = Color.web("#006666");
            systemLabel = "DIST";
        } else if (system instanceof MergeSystem) {
            systemColor = Color.web("#660066");
            systemLabel = "MERGE";
        } else if (system instanceof SaboteurSystem) {
            systemColor = Color.web("#990000");
            systemLabel = "SABOTEUR";
        } else if (system instanceof VPNSystem) {
            systemColor = Color.web("#0033cc");
            systemLabel = "VPN";
        } else if (system instanceof SpySystem) {
            systemColor = Color.web("#cc9900");
            systemLabel = "SPY";
        } else if (system instanceof AntiTrojanSystem) {
            systemColor = Color.web("#00994d");
            systemLabel = "ANTI-T";
        } else if (system instanceof ReferenceSystem) {
            systemColor = Color.web("#333333");
            systemLabel = system.getId().contains("SOURCE") ? "SOURCE" : "DEST";
        }


        gc.setFill(systemColor);
        gc.fillRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);
        gc.setStroke(Color.web("#7a7f8a"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);

        // نمایش نوع سیستم
        if (!systemLabel.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Orbitron", 14));
            gc.fillText(systemLabel, x + 10, y + 20);
        }

        // اندیکاتور اتصال
        double indicatorSize = 10;
        double indicatorX = x + SYSTEM_WIDTH - indicatorSize - 5;
        double indicatorY = y + 5;
        if (system.isFullyConnected()) {
            gc.setFill(Color.web("#39FF14"));
            gc.setEffect(new DropShadow(15, Color.web("#39FF14")));
        } else {
            gc.setFill(Color.web("#4d0000"));
            gc.setEffect(null);
        }
        gc.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize);
        gc.setEffect(null);

        if (system instanceof AntiTrojanSystem) {
            AntiTrojanSystem ats = (AntiTrojanSystem) system;
            gc.setStroke(ats.isActive() ? Color.web("#00994d", 0.5) : Color.web("#ff3b3b", 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(ats.getCenterPosition().getX() - 150, ats.getCenterPosition().getY() - 150, 300, 300);
        }
    }


    private void drawPacket(Packet packet) {
        if (packet == null || packet.getVisualPosition() == null) return;
        Point2D pos = packet.getVisualPosition();
        double size = 10;

        // --- Packet Drawing Logic (unchanged) ---
        if (packet instanceof LargePacket) {
            size = 20;
            gc.setFill(Color.MAGENTA);
            gc.fillRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);

        } else if (packet instanceof BitPacket) {
            size = 5;
            int colorHash = ((BitPacket) packet).getParentLargePacketId().hashCode();
            gc.setFill(Color.rgb(Math.abs(colorHash % 255), Math.abs(colorHash * 31 % 255), Math.abs(colorHash * 17 % 255)));
            gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);

        } else if (packet instanceof ProtectedPacket) {
            if (protectedPacketImage != null) {
                // Draw the image if it was loaded successfully
                double imageSize = 24; // You can adjust the size
                gc.drawImage(protectedPacketImage, pos.getX() - imageSize / 2, pos.getY() - imageSize / 2, imageSize, imageSize);
            } else {
                // Fallback to the original drawing method if the image is missing
                gc.setFill(Color.CYAN);
                gc.fillRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
                gc.setEffect(new DropShadow(15, Color.CYAN));
                gc.setStroke(Color.WHITE);
                gc.strokeRect(pos.getX() - size / 2 - 2, pos.getY() - size / 2 - 2, size + 4, size + 4);
                gc.setEffect(null);
            }

        } else if (packet instanceof TrojanPacket) {
            size = 12;
            gc.setFill(Color.RED);
            double[] xPoints = {pos.getX(), pos.getX() - size, pos.getX() + size};
            double[] yPoints = {pos.getY() + size/2, pos.getY() - size/2, pos.getY() - size/2};
            gc.fillPolygon(xPoints, yPoints, 3);

        } else if (packet instanceof SquarePacket) {
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        } else if (packet instanceof TrianglePacket) {
            gc.setFill(Color.YELLOW);
            double[] xPoints = {pos.getX(), pos.getX() - size / 2, pos.getX() + size / 2};
            double[] yPoints = {pos.getY() - size / 2, pos.getY() + size / 2, pos.getY() + size / 2};
            gc.fillPolygon(xPoints, yPoints, 3);
        } else if (packet instanceof CirclePacket) {
            gc.setFill(Color.ORANGE);
            gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        }
        else if (packet instanceof ProtectedConfidentialPacket) {
            if (protectedConfidentialPacketImage != null) {
                double imageSize = 24;
                gc.drawImage(protectedConfidentialPacketImage, pos.getX() - imageSize / 2, pos.getY() - imageSize / 2, imageSize, imageSize);
            } else {
                // Fallback: استفاده از تصویر ProtectedPacket یا رنگ آبی تیره
                Image fallback = protectedPacketImage != null ? protectedPacketImage : null;
                if (fallback != null) {
                    double imageSize = 24;
                    gc.drawImage(fallback, pos.getX() - imageSize / 2, pos.getY() - imageSize / 2, imageSize, imageSize);
                } else {
                    gc.setFill(Color.CYAN.darker());
                    gc.fillRect(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
                }
            }
        }
        else if (packet instanceof ConfidentialPacket) {
            if (confidentialPacketImage != null) {
                double imageSize = 18;
                gc.drawImage(confidentialPacketImage, pos.getX() - imageSize / 2, pos.getY() - imageSize / 2, imageSize, imageSize);
            } else {
                gc.setFill(Color.web("#a0a0a0", 0.7));
                gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
            }
        }
        else {
            gc.setFill(Color.WHITE);
            gc.fillOval(pos.getX() - size / 2, pos.getY() - size / 2, size, size);
        }

        // START: Health Bar Logic
        if (packet.getSize() > 0) {
            double noisePercentage = Math.min(1.0, packet.getNoise() / packet.getSize());
            double healthPercentage = 1.0 - noisePercentage;

            Color healthColor = Color.RED.interpolate(Color.LIMEGREEN, healthPercentage);

            double barWidth = 20;
            double barHeight = 4;
            double barY = pos.getY() - (size / 2) - 8;
            double barX = pos.getX() - barWidth / 2;

            // Draw the background
            gc.setFill(Color.BLACK);
            gc.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

            // Draw the used/lost health part (gray)
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(barX, barY, barWidth, barHeight);

            // Draw the remaining health
            if (healthPercentage > 0) {
                gc.setFill(healthColor);
                gc.fillRect(barX, barY, barWidth * healthPercentage, barHeight);
            }
        }
        // END: Health Bar Logic
    }

    private void drawPort(Port port) {
        if (port.getPosition() == null) return;
        double x = port.getPosition().getX();
        double y = port.getPosition().getY();
        Color portColor = getColorForShape(port.getShape());
        gc.setEffect(new DropShadow(10, portColor));
        gc.setFill(portColor);
        switch (port.getShape()) {
            case SQUARE:
                gc.fillRect(x, y, PORT_SIZE, PORT_SIZE);
                break;
            case TRIANGLE:
                double[] xPoints = {x + PORT_SIZE / 2, x, x + PORT_SIZE};
                double[] yPoints = {y, y + PORT_SIZE, y + PORT_SIZE};
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
            for (Port port : system.getInputPorts()) {
                if (port.getCenterPosition().distance(x, y) < PORT_SIZE) return port;
            }
            for (Port port : system.getOutputPorts()) {
                if (port.getCenterPosition().distance(x, y) < PORT_SIZE) return port;
            }
        }
        return null;
    }

    private Color getColorForShape(PortShape shape) {
        switch (shape) {
            case SQUARE: return Color.web("#00f0ff");
            case TRIANGLE: return Color.web("#ff00ff");
            case CIRCLE: return Color.ORANGE;
            default: return Color.WHITE;
        }
    }

    public void onShopClicked() {
        ScreenController.getInstance().activate(Screen.SHOP);
    }

    public void onPauseClicked() {
    }

    public void onMenuClicked() {
        gameLoop.stop();
        SaveManager.deleteAutoSave();
        ScreenController.getInstance().activate(Screen.MAIN_MENU);
    }

    @FXML
    public void onRunClicked() {
        if (currentPhase == GamePhase.DESIGN && isNetworkReady()) {
            currentPhase = GamePhase.SIMULATION;
            runButton.setDisable(true);
        }
    }
}