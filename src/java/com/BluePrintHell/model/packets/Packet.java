package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem; // import
import javafx.geometry.Point2D;

public abstract class Packet {
    protected Point2D position;
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected Point2D velocity;
    protected double noise = 0; // Field for tracking noise

    public Packet(Point2D startPosition) {
        this.position = startPosition;
        this.velocity = Point2D.ZERO;
        this.currentSpeed = 0;
    }

    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
        // The specific speed will be set by the subclass
    }

    public void update(double deltaTime) {
        if (destinationPort != null) {
            Point2D destCenter = new Point2D(
                    destinationPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    destinationPort.getPosition().getY() + 6
            );

            Point2D direction = destCenter.subtract(position).normalize();
            this.velocity = direction.multiply(currentSpeed); // Update velocity based on current speed
            this.position = position.add(velocity.multiply(deltaTime));

            if (position.distance(destCenter) < 2.0) {
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this);
                this.getParentGameState().removePacket(this);
                this.destinationPort = null;
                this.currentConnection = null;
            }
        }
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

    public abstract int getSize();

    public void addNoise(double amount) {
        this.noise += amount;
    }

    public void applyForce(Point2D force) {
        this.velocity = this.velocity.add(force);
    }
}