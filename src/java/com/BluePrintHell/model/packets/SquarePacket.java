package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;
public class SquarePacket extends Packet {
    private static final double BASE_SPEED = 100;

    public SquarePacket(Point2D startPosition) {
        super(startPosition, 2);
    }

    public SquarePacket() {
        super(null, 2);
    }

    @Override
    public void launch(Connection connection) {
        super.launch(connection);
        // Set speed based on compatibility
        if (isCompatibleWith(connection.getStartPort().getShape())) {
            this.currentSpeed = BASE_SPEED / 2.0; // Compatible: half speed
        } else {
            this.currentSpeed = BASE_SPEED; // Incompatible: normal speed
        }
    }

    @Override
    public int getCoinValue() { return 2; }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return shape == PortShape.SQUARE;
    }

    @Override
    public int getSize() {
        return 2; // Size defined in the documentation
    }
}