package com.BluePrintHell.model;

import com.BluePrintHell.model.network.NetworkSystem;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javafx.geometry.Point2D;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Port {
    private String id;
    private NetworkSystem parentSystem;
    private PortType type;
    private PortShape shape;
    private Point2D position;
    private Connection attachedConnection;
    private static final double PORT_SIZE = 12;

    public Port(String id, NetworkSystem parentSystem, PortType type, PortShape shape) {
        this.id = id;
        this.parentSystem = parentSystem;
        this.type = type;
        this.shape = shape;
    }

    private Port() { }

    public boolean isConnected() { return attachedConnection != null; }
    public void connect(Connection connection) { this.attachedConnection = connection; }
    public void disconnect() { this.attachedConnection = null; }

    public String getId() { return id; }
    @JsonIgnore
    public NetworkSystem getParentSystem() { return parentSystem; }
    public void setParentSystem(NetworkSystem parentSystem) { this.parentSystem = parentSystem; }
    public PortType getType() { return type; }
    public PortShape getShape() { return shape; }
    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }
    public Connection getAttachedConnection() { return attachedConnection; }
    public Point2D getCenterPosition() {
        if (position == null) return Point2D.ZERO;
        return new Point2D(position.getX() + PORT_SIZE / 2, position.getY() + PORT_SIZE / 2);
    }
}