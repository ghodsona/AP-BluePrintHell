package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

import java.util.Random;

public class LargePacketTypeB extends LargePacket {

    private static final double DEVIATION_DISTANCE = 50.0; // هر 50 واحد مسافت، یک انحراف رخ می‌دهد
    private static final double DEVIATION_AMOUNT = 15.0;  // حداکثر میزان انحراف از مرکز سیم
    private double distanceTraveledSinceLastDeviation = 0;
    private final Random random = new Random();

    public LargePacketTypeB(Point2D startPosition) {
        super(startPosition, 10); // اندازه: ۱۰ واحد
        this.currentSpeed = 70; // سرعت ثابت
    }

    public LargePacketTypeB() {
        super(null, 10);
    }

    @Override
    public void update(double deltaTime) {
        Point2D oldPosition = getPosition();
        // ابتدا حرکت عادی پکت را بر اساس سرعت ثابت انجام می‌دهیم
        super.update(deltaTime);
        Point2D newPosition = getPosition();

        // محاسبه مسافت طی شده در این فریم
        if (oldPosition != null && newPosition != null) {
            distanceTraveledSinceLastDeviation += oldPosition.distance(newPosition);
        }

        // بررسی اینکه آیا زمان انحراف فرا رسیده است یا نه
        if (distanceTraveledSinceLastDeviation >= DEVIATION_DISTANCE) {
            applyDeviation();
            distanceTraveledSinceLastDeviation = 0; // شمارنده را ریست می‌کنیم
        }
    }

    private void applyDeviation() {
        // یک جهت تصادفی برای انحراف عمود بر مسیر حرکت ایجاد می‌کند
        // (این بخش می‌تواند در آینده برای انحراف دقیق‌تر بهبود یابد)
        double offsetX = (random.nextDouble() - 0.5) * DEVIATION_AMOUNT;
        double offsetY = (random.nextDouble() - 0.5) * DEVIATION_AMOUNT;

        Point2D newPosition = getPosition().add(new Point2D(offsetX, offsetY));
        setPosition(newPosition);
        System.out.println("LargePacketTypeB deviated!");
    }

    @Override
    public int getCoinValue() {
        return 10;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return false;
    }
}