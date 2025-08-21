package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class TrianglePacket extends Packet {
    public TrianglePacket(Point2D startPosition) {
        super(startPosition);
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return shape == PortShape.TRIANGLE;
    }

    @Override
    public int getCoinValue() {
        return 3;
    }
}