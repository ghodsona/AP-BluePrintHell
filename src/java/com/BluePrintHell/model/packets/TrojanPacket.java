package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class TrojanPacket extends Packet {
    private static final double BASE_SPEED = 100;

    public TrojanPacket(Point2D startPosition) {
        super(startPosition, 2);
        this.currentSpeed = BASE_SPEED;
    }

    public TrojanPacket() {
        super(null, 2);
    }

    @Override
    public int getCoinValue() {
        return -5;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return false;
    }

    @Override
    public int getSize() {
        return 2;
    }
}