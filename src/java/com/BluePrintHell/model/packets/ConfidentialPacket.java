package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.network.NetworkSystem;
import javafx.geometry.Point2D;

public class ConfidentialPacket extends Packet {
    private static final double BASE_SPEED = 100;
    private static final double REDUCED_SPEED_FACTOR = 0.5; // کاهش سرعت به نصف
    private static final int COIN_VALUE = 3;
    private static final int PACKET_SIZE = 4;

    public ConfidentialPacket(Point2D startPosition) {
        super(startPosition, PACKET_SIZE);
        this.currentSpeed = BASE_SPEED;
    }

    public ConfidentialPacket() {
        super(null, PACKET_SIZE);
    }

    @Override
    public void update(double deltaTime) {
        // ۱. بررسی می‌کنیم که پکت مقصد (Destination Port) دارد
        if (destinationPort != null) {
            NetworkSystem destSystem = destinationPort.getParentSystem();

            // ۲. بررسی حیاتی: مطمئن می‌شویم که سیستم والد پورت مقصد نال نیست.
            if (destSystem != null) {
                boolean isBufferEmpty = destSystem.isBufferEmpty();
                double targetSpeed;
                String action;
                // تعریف واضح وضعیت بافر برای استفاده در خروجی JSON
                String bufferStatus = isBufferEmpty ? "EMPTY" : "NOT_EMPTY";

                // منطق اصلی کاهش سرعت
                if (!isBufferEmpty) {
                    targetSpeed = BASE_SPEED * REDUCED_SPEED_FACTOR;
                    action = "SLOWDOWN_APPLIED";
                } else {
                    targetSpeed = BASE_SPEED;
                    action = "NORMAL_SPEED";
                }

                this.currentSpeed = targetSpeed;

                // --- خروجی دیباگ JSON برای تست ---
                // حالا از متغیرهای bufferStatus و action که قبلا تعریف شده‌اند استفاده می‌کنیم
                String jsonOutput = String.format(
                        "{\"packet_hash\": %d, \"current_speed\": %.2f, \"dest_system\": \"%s\", \"buffer_status\": \"%s\", \"action\": \"%s\"}",
                        this.hashCode(),
                        this.currentSpeed,
                        destSystem.getId(),
                        bufferStatus, // متغیر واضح برای وضعیت بافر
                        action       // متغیر واضح برای اکشن
                );
                System.out.println("DEBUG_CONFIDENTIAL_PACKET_SPEED: " + jsonOutput);
                // ------------------------------------
            }
        }
        super.update(deltaTime);
    }

    @Override
    public int getCoinValue() {
        return COIN_VALUE;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return false;
    }
}