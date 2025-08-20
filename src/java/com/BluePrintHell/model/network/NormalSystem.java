package com.BluePrintHell.model.network;

import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import javafx.geometry.Point2D;

public class NormalSystem extends NetworkSystem {
    public NormalSystem(String id, Point2D position) { super(id, position); }

    @Override
    public void update(double deltaTime) {
        // اگر پکتی در بافر منتظر است
        if (!packetBuffer.isEmpty()) {
            // یک پورت خروجی خالی و متصل پیدا کن
            for (Port outputPort : outputPorts) {
                if (outputPort.isConnected()) { // && سیم خالی است (این منطق بعدا اضافه می‌شود)
                    // پکت را از بافر بردار
                    Packet packetToLaunch = packetBuffer.poll();

                    if (packetToLaunch != null) {
                        // موقعیت پکت را به مرکز پورت خروجی منتقل کن
                        Point2D newPosition = new Point2D(
                                outputPort.getPosition().getX() + 6, // PORT_SIZE / 2
                                outputPort.getPosition().getY() + 6  // PORT_SIZE / 2
                        );
                        packetToLaunch.setPosition(newPosition);

                        // پکت را روی سیم راهی کن
                        packetToLaunch.launch(outputPort.getAttachedConnection());

                        // پکت را دوباره به لیست پکت‌های فعال بازی برگردان
                        this.getParentGameState().addPacket(packetToLaunch);
                        break; // فقط یک پکت در هر فریم ارسال می‌شود
                    }
                }
            }
        }
    }
}