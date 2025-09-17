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

    public List<Point2D> getPathPoints() {
        List<Point2D> pathPoints = new ArrayList<>();
        pathPoints.add(startPort.getCenterPosition());
        pathPoints.addAll(bendPoints);
        pathPoints.add(endPort.getCenterPosition());
        return pathPoints;
    }

    public double calculateLength() {
        List<Point2D> path = getPathPoints();
        if (path.size() < 2) return 0;

        double totalLength = 0;
        for(int i = 0; i < path.size() - 1; i++){
            totalLength += path.get(i).distance(path.get(i+1));
        }
        return totalLength;
    }

    public Point2D getPointOnCurve(double t) {
        List<Point2D> points = getPathPoints();
        int n = points.size() - 1;
        if (n < 1) return startPort.getCenterPosition();

        double scaled_t = t * n;
        int segment = Math.min((int)scaled_t, n - 1);
        double local_t = scaled_t - segment;

        Point2D p0 = points.get(segment);
        Point2D p1 = points.get(segment + 1);

        Point2D control1 = (segment > 0) ? points.get(segment-1) : p0;
        Point2D control2 = (segment < n-1) ? points.get(segment+2) : p1;

        // Catmull-Rom to Cubic Bezier conversion for control points
        Point2D c1 = p0.add(p1.subtract(control1).multiply(1.0/6.0));
        Point2D c2 = p1.subtract(control2.subtract(p0).multiply(1.0/6.0));

        // Cubic Bezier formula
        double omt = 1.0 - local_t;
        double omt2 = omt * omt;
        double omt3 = omt2 * omt;
        double t2 = local_t * local_t;
        double t3 = t2 * local_t;

        double x = omt3 * p0.getX() + 3 * omt2 * local_t * c1.getX() + 3 * omt * t2 * c2.getX() + t3 * p1.getX();
        double y = omt3 * p0.getY() + 3 * omt2 * local_t * c1.getY() + 3 * omt * t2 * c2.getY() + t3 * p1.getY();

        return new Point2D(x, y);
    }

    public double getDistanceFromPoint(Point2D point) {
        List<Point2D> path = getPathPoints();
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
    public Port getEndPort() { return endPort; }
    public List<Point2D> getBendPoints() { return bendPoints; }
}