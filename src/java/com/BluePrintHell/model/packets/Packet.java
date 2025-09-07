package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

public abstract class Packet {
    // The logical point that stays on the wire
    protected Point2D anchorPosition;
    // The visual displacement from the anchor, caused by impacts
    protected Point2D visualOffset;

    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected Point2D velocity;
    protected double noise;

    public Packet(Point2D startPosition) {
        this.anchorPosition = startPosition;
        this.visualOffset = Point2D.ZERO;
        this.velocity = Point2D.ZERO;
        this.currentSpeed = 0;
        this.noise = 0;
    }

    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
        if (this.currentConnection != null) {
            this.currentConnection.setPacketOnWire(this);
        }
    }

    public void update(double deltaTime) {
        // --- 1. Move the anchor point along the wire ---
        if (destinationPort != null) {
            Point2D destCenter = new Point2D(
                    destinationPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    destinationPort.getPosition().getY() + 6
            );

            Point2D direction = destCenter.subtract(anchorPosition).normalize();
            this.velocity = direction.multiply(currentSpeed);
            this.anchorPosition = anchorPosition.add(velocity.multiply(deltaTime));

            // --- Handle arrival at destination ---
            if (anchorPosition.distance(destCenter) < 2.0) {
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this);

                if (this.currentConnection != null) {
                    this.currentConnection.clearPacket();
                }

                this.getParentGameState().removePacket(this);

                this.destinationPort = null;
                this.currentConnection = null;
            }
        }

        // --- 2. Gradually reduce the visual offset back to zero ---
        if (visualOffset.magnitude() > 0.1) {
            visualOffset = visualOffset.multiply(0.95);
        } else {
            visualOffset = Point2D.ZERO;
        }
    }

    public void applyForce(Point2D force) {
        this.visualOffset = this.visualOffset.add(force);
    }

    // --- Position Management ---
    public void setPosition(Point2D position) {
        this.anchorPosition = position;
    }

    public Point2D getPosition() {
        return anchorPosition;
    }

    public Point2D getVisualPosition() {
        return anchorPosition.add(visualOffset);
    }

    // --- Abstract Methods ---
    public abstract int getCoinValue();
    public abstract int getSize();
    public abstract boolean isCompatibleWith(PortShape shape);

    // --- Other Methods ---
    public void addNoise(double amount) {
        this.noise += amount;
    }

    protected GameState getParentGameState() {
        if (currentConnection != null && currentConnection.getStartPort() != null &&
                currentConnection.getStartPort().getParentSystem() != null) {
            return currentConnection.getStartPort().getParentSystem().getParentGameState();
        }
        return null;
    }
}