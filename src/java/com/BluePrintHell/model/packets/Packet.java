package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public abstract class Packet {
    protected Point2D position;
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected Point2D velocity;
    protected double noise = 0;
    private final int size;
    private static final double DEVIATION_THRESHOLD = 15.0;
    private List<Point2D> path;
    private int currentPathIndex;


    public Connection getCurrentConnection() {
        return currentConnection;
    }

    public Packet(Point2D startPosition, int size) {
        this.position = startPosition;
        this.velocity = Point2D.ZERO;
        this.currentSpeed = 0;
        this.size = size;
        this.path = new ArrayList<>();
        this.currentPathIndex = 0;
    }

    public Point2D getVisualPosition() {
        return position;
    }


    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();

        this.path.clear();
        this.path.add(connection.getStartPort().getCenterPosition());
        this.path.addAll(connection.getBendPoints());
        this.path.add(connection.getEndPort().getCenterPosition());

        this.currentPathIndex = 0;
    }

    public void update(double deltaTime) {
        if (isLost()) {
            GameState gs = getParentGameState();
            if (gs != null) {
                gs.losePacket(this);
            }
            return;
        }
        if (path == null || currentPathIndex >= path.size() - 1) {
            if (destinationPort != null) {
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this);
                getParentGameState().removePacket(this);
                this.destinationPort = null;
                this.currentConnection = null;
            }
            return;
        }

        Point2D target = path.get(currentPathIndex + 1);

        Point2D direction = target.subtract(position).normalize();
        this.velocity = direction.multiply(currentSpeed);
        this.position = position.add(velocity.multiply(deltaTime));

        if (position.distance(target) < 2.0) {
            currentPathIndex++;
            this.position = target;
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

    public int getSize() {
        return this.size;
    }

    public void addNoise(double amount) {
        this.noise += amount;
        System.out.println("INFO: Noise added. Current noise: " + this.noise + ", Size: " + this.size);
    }

    public boolean isLost() {
        boolean lostByNoise = noise > size;
        if (lostByNoise) {
            System.out.println("PACKET LOST: Noise exceeded size.");
            return true;
        }

        if (currentConnection != null) {
            double deviation = currentConnection.getDistanceFromPoint(this.position);
            if (deviation > DEVIATION_THRESHOLD) {
                System.out.println("PACKET LOST: Deviated from wire. Distance: " + deviation);
                return true;
            }
        }

        return false;
    }

    public void applyForce(Point2D force) {
        this.velocity = this.velocity.add(force);
    }
}