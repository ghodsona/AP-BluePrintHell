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
        if (bendPoints.isEmpty()) {
            return startPort.getCenterPosition().distance(endPort.getCenterPosition());
        }

        double totalLength = 0;
        Point2D currentPoint = startPort.getCenterPosition();

        for (Point2D bendPoint : bendPoints) {
            totalLength += currentPoint.distance(bendPoint);
            currentPoint = bendPoint;
        }

        totalLength += currentPoint.distance(endPort.getCenterPosition());
        return totalLength;
    }

    public double getDistanceFromPoint(Point2D point) {
        List<Point2D> pathPoints = new ArrayList<>();
        pathPoints.add(startPort.getCenterPosition());
        pathPoints.addAll(bendPoints);
        pathPoints.add(endPort.getCenterPosition());

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Point2D p1 = pathPoints.get(i);
            Point2D p2 = pathPoints.get(i + 1);
            double distanceToSegment = getDistanceToLineSegment(p1, p2, point);
            if (distanceToSegment < minDistance) {
                minDistance = distanceToSegment;
            }
        }

        return minDistance;
    }

    private double getDistanceToLineSegment(Point2D p1, Point2D p2, Point2D point) {
        // ✅✅✅ اصلاحیه اصلی اینجاست ✅✅✅
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        // محاسبه فاصله به توان دو به صورت دستی
        double l2 = dx * dx + dy * dy;

        if (l2 == 0.0) return point.distance(p1);

        double t = ((point.getX() - p1.getX()) * dx + (point.getY() - p1.getY()) * dy) / l2;
        t = Math.max(0, Math.min(1, t));

        Point2D closestPoint = new Point2D(
                p1.getX() + t * dx,
                p1.getY() + t * dy
        );

        return point.distance(closestPoint);
    }

    // --- Getters ---
    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }
    public List<Point2D> getBendPoints() { return bendPoints; }
}