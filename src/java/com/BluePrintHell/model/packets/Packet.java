package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.network.NetworkSystem; // import
import javafx.geometry.Point2D;

public abstract class Packet {
    protected Point2D position; // این متغیر از این به بعد "نقطه ثقل" پکت است
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double speed = 150; // سرعت پیش‌فرض: ۱۵۰ پیکسل بر ثانیه

    public Packet(Point2D startPosition) {
        this.position = startPosition;
    }

    /**
     * این متد جدید، پکت را روی یک سیم برای حرکت به سمت مقصد آماده می‌کند.
     */
    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
        System.out.println("Packet launched towards: " + destinationPort.getId());
    }


    public void update(double deltaTime) {
        // فقط اگر پکت یک مقصد داشته باشد، حرکت کن
        if (destinationPort != null) {
            Point2D destCenter = new Point2D(
                    destinationPort.getPosition().getX() + 12 / 2.0, // PORT_SIZE / 2
                    destinationPort.getPosition().getY() + 12 / 2.0
            );

            // ۱. بردار جهت به سمت مقصد را پیدا کن
            Point2D direction = destCenter.subtract(position).normalize();

            // ۲. بردار سرعت را بساز
            Point2D velocity = direction.multiply(speed);

            // ۳. پکت را به اندازه یک گام کوچک حرکت بده
            position = position.add(velocity.multiply(deltaTime));

            // ۴. چک کن آیا به مقصد رسیده‌ای یا نه
            if (position.distance(destCenter) < 2.0) { // یک محدوده کوچک برای رسیدن
                System.out.println("Packet arrived at: " + destinationPort.getId());
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this); // خودت را به سیستم مقصد تحویل بده

                // وضعیت حرکت خود را ریست کن
                this.destinationPort = null;
                this.currentConnection = null;
            }
        }
    }

    // --- Getters and Setters ---
    public Point2D getPosition() { return position; }
    public void setVelocity(Point2D velocity) { /* این متد دیگر لازم نیست */ }

    public void setPosition(Point2D position) {
        this.position = position;
    }
}