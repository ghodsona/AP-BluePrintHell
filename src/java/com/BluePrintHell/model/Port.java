package com.BluePrintHell.model;

import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

public class Port {
    private final String id;
    private final NetworkSystem parentSystem;
    private final PortType type;
    private final PortShape shape;
    private Point2D position; // موقعیت دقیق روی صفحه
    private Connection attachedConnection;
    private static final double PORT_SIZE = 12; // اندازه پورت را به عنوان ثابت تعریف می‌کنیم

    public Port(String id, NetworkSystem parentSystem, PortType type, PortShape shape) {
        this.id = id;
        this.parentSystem = parentSystem;
        this.type = type;
        this.shape = shape;
        // موقعیت دقیق بعداً محاسبه می‌شود
    }

    public boolean isConnected() {
        return attachedConnection != null;
    }

    public void connect(Connection connection) {
        this.attachedConnection = connection;
    }

    public Point2D getCenterPosition() {
        if (position == null) return Point2D.ZERO;
        return new Point2D(position.getX() + PORT_SIZE / 2, position.getY() + PORT_SIZE / 2);
    }

    public void disconnect() {
        this.attachedConnection = null;
    }

    // --- Getters ---
    public String getId() { return id; }
    public NetworkSystem getParentSystem() { return parentSystem; }
    public PortType getType() { return type; }
    public PortShape getShape() { return shape; }
    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }
    public Connection getAttachedConnection() { return attachedConnection; }
}