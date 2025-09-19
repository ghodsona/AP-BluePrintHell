package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javafx.geometry.Point2D;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "packetType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SquarePacket.class, name = "SquarePacket"),
        @JsonSubTypes.Type(value = TrianglePacket.class, name = "TrianglePacket"),
        @JsonSubTypes.Type(value = CirclePacket.class, name = "CirclePacket")
})

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public abstract class Packet {
    protected Point2D position;
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected double noise = 0;
    private final int size;
    private static final double DEVIATION_THRESHOLD = 15.0;

    private List<Point2D> path;
    private int currentPathIndex;

    public Packet(Point2D startPosition, int size) {
        this.position = startPosition;
        this.currentSpeed = 0;
        this.size = size;
    }

    public Connection getCurrentConnection() {
        return currentConnection;
    }

    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
        this.path = connection.getSmoothPath(20);
        this.currentPathIndex = 0;
        if (!this.path.isEmpty()) {
            this.position = this.path.get(0);
        }
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
        double distanceToTarget = position.distance(target);
        double travelDistance = currentSpeed * deltaTime;

        while (travelDistance >= distanceToTarget && currentPathIndex < path.size() - 1) {
            travelDistance -= distanceToTarget;
            position = target;
            currentPathIndex++;
            if (currentPathIndex >= path.size() - 1) {
                break;
            }
            target = path.get(currentPathIndex + 1);
            distanceToTarget = position.distance(target);
        }

        if (currentPathIndex < path.size() - 1 && distanceToTarget > 0) {
            Point2D direction = target.subtract(position).normalize();
            position = position.add(direction.multiply(travelDistance));
        } else if (currentPathIndex >= path.size() - 1) {
            position = path.get(path.size() - 1);
        }
    }

    public void applyForce(Point2D force) {
        this.position = this.position.add(force.multiply(0.1));
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

    public abstract int getCoinValue();
    public abstract boolean isCompatibleWith(PortShape shape);
    public Point2D getVisualPosition() { return position; }
    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }
    protected GameState getParentGameState() {
        if(currentConnection != null) {
            return currentConnection.getStartPort().getParentSystem().getParentGameState();
        }
        return null;
    }
    public int getSize() { return this.size; }
    public void addNoise(double amount) { this.noise += amount; }
    public double getNoise() { return this.noise; }
}