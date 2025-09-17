package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

public abstract class Packet {
    protected Point2D position;
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected Point2D velocity;
    protected double noise = 0;
    private final int size;
    private static final double DEVIATION_THRESHOLD = 15.0;

    private double progressOnPath = 0.0;
    private double pathLength = 0.0;

    public Packet(Point2D startPosition, int size) {
        this.position = startPosition;
        this.velocity = Point2D.ZERO;
        this.currentSpeed = 0;
        this.size = size;
    }

    public Connection getCurrentConnection() {
        return currentConnection;
    }

    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
        this.pathLength = connection.calculateLength();
        this.progressOnPath = 0.0;
    }

    public void update(double deltaTime) {
        if (isLost()) {
            GameState gs = getParentGameState();
            if (gs != null) {
                gs.losePacket(this);
            }
            return;
        }

        if (currentConnection != null) {
            double step = (currentSpeed / pathLength) * deltaTime;
            progressOnPath += step;

            if (progressOnPath >= 1.0) {
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this);
                getParentGameState().removePacket(this);
                this.destinationPort = null;
                this.currentConnection = null;
            } else {
                this.position = currentConnection.getPointOnCurve(progressOnPath);
            }
        }
    }

    public Point2D getVisualPosition() {
        return position;
    }

    public abstract int getCoinValue();
    public abstract boolean isCompatibleWith(PortShape shape);

    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }

    protected GameState getParentGameState() {
        if(currentConnection != null) {
            return currentConnection.getStartPort().getParentSystem().getParentGameState();
        }
        return null;
    }

    public int getSize() {
        return this.size;
    }

    public void addNoise(double amount) {
        this.noise += amount;
    }

    public boolean isLost() {
        boolean lostByNoise = noise > size;
        if (lostByNoise) return true;

        if (currentConnection != null) {
            double deviation = currentConnection.getDistanceFromPoint(this.position);
            if (deviation > DEVIATION_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    public void applyForce(Point2D force) {
        // این نیرو مستقیماً موقعیت را جابجا می‌کند تا انحراف ایجاد شود
        this.position = this.position.add(force.multiply(0.1));
    }
}