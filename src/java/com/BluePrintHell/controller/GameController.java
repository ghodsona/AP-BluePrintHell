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
import com.BluePrintHell.util.CollisionManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
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
        this.gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setOnMousePressed(this::onMousePressed);
        gameCanvas.setOnMouseDragged(this::onMouseDragged);
        gameCanvas.setOnMouseReleased(this::onMouseReleased);
        gameCanvas.setOnMouseMoved(this::onMouseMoved);
        calculateInitialPositions();
        setupGameLoop();
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
            if (isConnectionValid(selectedConnectionForBending)) {
                selectedBendPoint = currentPoint;
            } else {
                selectedConnectionForBending.getBendPoints().set(selectedBendPointIndex, originalPosition);
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
                if (isConnectionValid(newConnection) && getTotalWireLength() + newConnection.calculateLength() <= gameState.getPlayerWireLength()) {
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

    private boolean isSegmentIntersectingAnySystem(Point2D p1, Point2D p2, Port startPort, Port endPort) {
        for (NetworkSystem system : gameState.getSystems()) {
            if (system == startPort.getParentSystem() || (endPort != null && system == endPort.getParentSystem())) {
                continue;
            }
            double sysX = system.getPosition().getX();
            double sysY = system.getPosition().getY();
            if (new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY()).intersects(sysX, sysY, SYSTEM_WIDTH, SYSTEM_HEIGHT)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        List<Point2D> pathPoints = new ArrayList<>();
        pathPoints.add(conn.getStartPort().getCenterPosition());
        pathPoints.addAll(conn.getBendPoints());
        pathPoints.add(conn.getEndPort().getCenterPosition());
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            if (isSegmentIntersectingAnySystem(pathPoints.get(i), pathPoints.get(i + 1), conn.getStartPort(), conn.getEndPort())) {
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

        List<Point2D> points = connection.getPathPoints();
        if (points.size() < 2) return;

        gc.beginPath();
        gc.moveTo(points.get(0).getX(), points.get(0).getY());

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p0 = points.get(i);
            Point2D p1 = points.get(i + 1);

            Point2D control1 = (i > 0) ? points.get(i - 1) : p0;
            Point2D control2 = (i < points.size() - 2) ? points.get(i + 2) : p1;

            // Catmull-Rom to Cubic Bezier conversion for control points
            Point2D c1 = p0.add(p1.subtract(control1).multiply(1.0 / 6.0));
            Point2D c2 = p1.subtract(control2.subtract(p0).multiply(1.0 / 6.0));

            gc.bezierCurveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), p1.getX(), p1.getY());
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
        double previewLength = start.distance(liveWireEndPoint);
        if (getTotalWireLength() + previewLength > gameState.getPlayerWireLength() || isSegmentIntersectingAnySystem(start, liveWireEndPoint, wireStartPort, null)) {
            gc.setStroke(Color.RED);
        } else {
            gc.setStroke(Color.web("#00f0ff", 0.9));
        }
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
        double remainingWire = gameState.getPlayerWireLength() - getTotalWireLength();
        wireLengthLabel.setText(String.format("Wire Length: %.0f", remainingWire));
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

    private boolean isNetworkReady() {
        if (gameState == null) return false;
        for (NetworkSystem system : gameState.getSystems()) {
            if (!system.isFullyConnected()) {
                return false;
            }
        }
        return true;
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
            case "TRIANGLE_MESSENGER": return new TrianglePacket(position);
            case "CIRCLE_MESSENGER": return new CirclePacket(position);
            default: return null;
        }
    }

    private void handlePacketSpawning() {
        if (gameState.getSpawnEvents().isEmpty()) return;
        SpawnEventData nextEvent = gameState.getSpawnEvents().get(0);
        if (gameState.getGameTime() >= nextEvent.getSpawnTime()) {
            NetworkSystem sourceSystem = gameState.getSystems().stream()
                    .filter(s -> s.getId().equals(nextEvent.getSourceSystemId())).findFirst().orElse(null);
            if (sourceSystem != null) {
                Packet newPacket = createPacketFromType(nextEvent.getPacketType(), new Point2D(0, 0));
                if (newPacket != null) {
                    sourceSystem.receivePacket(newPacket);
                    gameState.incrementTotalPacketsSpawned();
                }
            }
            gameState.getSpawnEvents().remove(0);
        }
    }

    private void drawSystem(NetworkSystem system) {
        double x = system.getPosition().getX();
        double y = system.getPosition().getY();
        gc.setFill(Color.web("#4a4f5a"));
        gc.fillRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);
        gc.setStroke(Color.web("#7a7f8a"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, SYSTEM_WIDTH, SYSTEM_HEIGHT, 10, 10);
        gc.setStroke(Color.web("#00f0ff", 0.5));
        gc.setLineWidth(3);
        gc.strokeLine(x + 5, y + 5, x + SYSTEM_WIDTH - 5, y + 5);
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
    }

    private void drawPacket(Packet packet) {
        if (packet == null) return;
        Point2D pos = packet.getVisualPosition();
        double size = 10;
        if (packet instanceof SquarePacket) {
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
        // TODO: Implement pause logic
    }

    public void onMenuClicked() {
        gameLoop.stop();
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