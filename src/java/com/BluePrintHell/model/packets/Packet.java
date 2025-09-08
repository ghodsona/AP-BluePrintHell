package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

public abstract class Packet {
    protected Point2D position;
    protected Connection currentConnection;
    protected Port destinationPort;
    protected double currentSpeed;
    protected Point2D velocity;
    protected double noise = 0;
    private final int size; // اندازه پکت برای مقایسه با نویز. از این به بعد final است

    // کانستراکتور آپدیت شد تا اندازه را به عنوان ورودی اصلی بگیرد
    public Packet(Point2D startPosition, int size) {
        this.position = startPosition;
        this.velocity = Point2D.ZERO;
        this.currentSpeed = 0;
        this.size = size;
    }

    public Point2D getVisualPosition() {
        return position;
    }

    public void launch(Connection connection) {
        this.currentConnection = connection;
        this.destinationPort = connection.getEndPort();
    }

    public void update(double deltaTime) {
        // اگر پکت شرایط از دست رفتن را داشت، آن را به GameState اطلاع می‌دهیم
        if (isLost()) {
            GameState gs = getParentGameState();
            if (gs != null) {
                gs.losePacket(this);
            }
            return; // آپدیت را متوقف می‌کنیم
        }

        if (destinationPort != null) {
            Point2D destCenter = new Point2D(
                    destinationPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    destinationPort.getPosition().getY() + 6
            );

            Point2D direction = destCenter.subtract(position).normalize();
            this.velocity = direction.multiply(currentSpeed);
            this.position = position.add(velocity.multiply(deltaTime));

            if (position.distance(destCenter) < 2.0) {
                NetworkSystem destSystem = destinationPort.getParentSystem();
                destSystem.receivePacket(this);
                getParentGameState().removePacket(this); // از لیست پکت‌های فعال حذف می‌شود
                this.destinationPort = null;
                this.currentConnection = null;
            }
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

    // این متد دیگر abstract نیست چون مقدار آن در کانستراکتور مشخص می‌شود
    public int getSize() {
        return this.size;
    }

    public void addNoise(double amount) {
        this.noise += amount;
        System.out.println("INFO: Noise added. Current noise: " + this.noise + ", Size: " + this.size);
    }

    // متد جدید برای چک کردن از بین رفتن پکت
    public boolean isLost() {
        // طبق داکیومنت: اگر میزان نویز از اندازه‌اش بیشتر شود، آن پکت از دست خواهد رفت
        return noise > size;
    }

    public void applyForce(Point2D force) {
        // این نیرو به صورت آنی به سرعت اضافه می‌شود
        this.velocity = this.velocity.add(force);
    }
}