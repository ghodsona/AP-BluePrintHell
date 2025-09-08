package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;
public class TrianglePacket extends Packet {
    private static final double BASE_SPEED = 120;
    private boolean isAccelerating = false;

    public TrianglePacket(Point2D startPosition) {
        super(startPosition, 3);
    }

    @Override
    public void launch(Connection connection) {
        super.launch(connection);
        // Set movement type based on compatibility
        if (isCompatibleWith(connection.getStartPort().getShape())) {
            this.currentSpeed = BASE_SPEED; // Compatible: constant speed
            this.isAccelerating = false;
        } else {
            this.currentSpeed = BASE_SPEED / 2.0; // Incompatible: start slow and accelerate
            this.isAccelerating = true;
        }
    }

    @Override
    public void update(double deltaTime) {
        if (isAccelerating) {
            this.currentSpeed += 50 * deltaTime; // Acceleration factor
        }
        super.update(deltaTime); // Call the parent update method to handle movement
    }

    @Override
    public int getCoinValue() { return 3; }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return shape == PortShape.TRIANGLE;
    }

    @Override
    public int getSize() {
        return 3; // Size defined in the documentation
    }
}