package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class ProtectedPacket extends Packet {
    private Packet originalPacket;
    private static final double BASE_SPEED = 110;

    public ProtectedPacket(Packet originalPacket) {
        super(originalPacket.getPosition(), originalPacket.getSize() * 2);
        this.originalPacket = originalPacket;
        this.currentSpeed = BASE_SPEED;
    }

    public ProtectedPacket() {
        super(null, 0);
    }

    public Packet getOriginalPacket() {
        return originalPacket;
    }

    @Override
    public int getCoinValue() {
        return originalPacket.getCoinValue();
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return true;
    }
}