package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import javafx.geometry.Point2D;

public abstract class Packet {
    protected Point2D position;
    protected Point2D velocity;
    protected Connection currentConnection; // سیمی که پکت روی آن حرکت می‌کند

    public Packet(Point2D startPosition) {
        this.position = startPosition;
        this.velocity = Point2D.ZERO; // سرعت اولیه صفر است
    }

    // هر پکت در هر فریم آپدیت می‌شود
    public void update(double deltaTime) {
        // منطق حرکت پایه
        position = position.add(velocity.multiply(deltaTime));
        // ... منطق پیچیده‌تر در کلاس‌های فرزند پیاده‌سازی می‌شود
    }

    // --- Getters and Setters ---
    public Point2D getPosition() { return position; }
    public void setVelocity(Point2D velocity) { this.velocity = velocity; }
    public void setCurrentConnection(Connection connection) { this.currentConnection = connection; }
}
