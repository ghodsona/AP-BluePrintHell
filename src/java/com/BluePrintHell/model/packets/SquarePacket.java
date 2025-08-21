package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class SquarePacket extends Packet {
    public SquarePacket(Point2D startPosition) {
        super(startPosition);
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return shape == PortShape.SQUARE;
    }

    @Override
    public int getCoinValue() {
        return 2;
    }
}