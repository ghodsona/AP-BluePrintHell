package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class CirclePacket extends Packet {
    private static final double BASE_SPEED = 130;
    private boolean isAccelerating;
    private double accelerationFactor;

    public CirclePacket(Point2D startPosition) {
        super(startPosition);
    }

    @Override
    public void launch(Connection connection) {
        super.launch(connection);
        if (isCompatibleWith(connection.getStartPort().getShape())) {
            // حرکت با شتاب ثابت
            this.isAccelerating = true;
            this.accelerationFactor = 40; // شتاب مثبت
            this.currentSpeed = BASE_SPEED / 2;
        } else {
            // حرکت با شتاب نزولی
            this.isAccelerating = true;
            this.accelerationFactor = -40; // شتاب منفی (کاهنده)
            this.currentSpeed = BASE_SPEED * 1.5; // با سرعت بالا شروع می‌شود
        }
    }

    @Override
    public void update(double deltaTime) {
        if (isAccelerating) {
            this.currentSpeed += accelerationFactor * deltaTime;
            if (this.currentSpeed < 0) this.currentSpeed = 0;
        }
        super.update(deltaTime);
    }

    @Override
    public int getCoinValue() {
        return 1;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return shape == PortShape.CIRCLE;
    }
}