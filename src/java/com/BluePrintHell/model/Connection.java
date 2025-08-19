package com.BluePrintHell.model;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Connection {
    private final Port startPort;
    private final Port endPort;
    private final List<Point2D> bendPoints = new ArrayList<>();

    public Connection(Port startPort, Port endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
    }

    public double calculateLength() {
        // TODO: منطق محاسبه طول سیم (با در نظر گرفتن نقاط انحنا)
        return startPort.getPosition().distance(endPort.getPosition());
    }

    // --- Getters ---
    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }
    public List<Point2D> getBendPoints() { return bendPoints; }
}