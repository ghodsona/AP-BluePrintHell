package com.BluePrintHell.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Connection {
    private Port startPort;
    private Port endPort;
    private List<Point2D> bendPoints = new ArrayList<>();

    public Connection(Port startPort, Port endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
        this.bendPoints = new ArrayList<>();
    }

    public Connection() {
        this.bendPoints = new ArrayList<>();
    }

    public List<Point2D> getPathPoints() {
        List<Point2D> pathPoints = new ArrayList<>();
        if (startPort != null && startPort.getCenterPosition() != null) {
            pathPoints.add(startPort.getCenterPosition());
        }
        if (bendPoints != null) {
            pathPoints.addAll(bendPoints);
        }
        if (endPort != null && endPort.getCenterPosition() != null) {
            pathPoints.add(endPort.getCenterPosition());
        }
        return pathPoints;
    }

    public List<Point2D> getSmoothPath(int segmentsPerCurve) {
        List<Point2D> points = getPathPoints();
        if (points.size() < 2) return points;

        if (points.size() == 2) {
            return points;
        }

        List<Point2D> smoothPath = new ArrayList<>();
        smoothPath.add(points.get(0));

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p0 = (i == 0) ? points.get(i) : points.get(i - 1);
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            Point2D p3 = (i + 2 > points.size() - 1) ? points.get(i + 1) : points.get(i + 2);

            for (int j = 1; j <= segmentsPerCurve; j++) {
                float t = (float) j / segmentsPerCurve;
                double x = 0.5 * ((2 * p1.getX()) + (-p0.getX() + p2.getX()) * t + (2 * p0.getX() - 5 * p1.getX() + 4 * p2.getX() - p3.getX()) * t * t + (-p0.getX() + 3 * p1.getX() - 3 * p2.getX() + p3.getX()) * t * t * t);
                double y = 0.5 * ((2 * p1.getY()) + (-p0.getY() + p2.getY()) * t + (2 * p0.getY() - 5 * p1.getY() + 4 * p2.getY() - p3.getY()) * t * t + (-p0.getY() + 3 * p1.getY() - 3 * p2.getY() + p3.getY()) * t * t * t);
                smoothPath.add(new Point2D(x, y));
            }
        }
        return smoothPath;
    }

    public double calculateLength() {
        List<Point2D> path = getSmoothPath(10);
        double totalLength = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalLength += path.get(i).distance(path.get(i + 1));
        }
        return totalLength;
    }

    public double getDistanceFromPoint(Point2D point) {
        List<Point2D> path = getSmoothPath(10);
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < path.size() - 1; i++) {
            double dist = getDistanceToLineSegment(path.get(i), path.get(i+1), point);
            if (dist < minDistance) {
                minDistance = dist;
            }
        }
        return minDistance;
    }

    private double getDistanceToLineSegment(Point2D p1, Point2D p2, Point2D point) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double l2 = dx * dx + dy * dy;
        if (l2 == 0.0) return point.distance(p1);
        double t = ((point.getX() - p1.getX()) * dx + (point.getY() - p1.getY()) * dy) / l2;
        t = Math.max(0, Math.min(1, t));
        Point2D closestPoint = new Point2D(p1.getX() + t * dx, p1.getY() + t * dy);
        return point.distance(closestPoint);
    }

    public Port getStartPort() { return startPort; }
    public void setStartPort(Port startPort) { this.startPort = startPort; }

    public Port getEndPort() { return endPort; }
    public void setEndPort(Port endPort) { this.endPort = endPort; }

    public List<Point2D> getBendPoints() { return bendPoints; }
    public void setBendPoints(List<Point2D> bendPoints) { this.bendPoints = bendPoints != null ? bendPoints : new ArrayList<>(); }
}