package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class TrojanPacket extends Packet {
    private static final double BASE_SPEED = 100;
    private Class<? extends Packet> originalPacketClass;

    public TrojanPacket(Packet originalPacket) {
        super(originalPacket.getPosition(), originalPacket.getSize());
        this.noise = originalPacket.getNoise(); // Inherit the noise
        this.originalPacketClass = originalPacket.getClass();
        this.currentSpeed = BASE_SPEED;
    }

    public TrojanPacket() { // Needed for deserialization
        super(null, 2);
    }

    public Class<? extends Packet> getOriginalPacketClass() {
        return originalPacketClass;
    }

    @Override
    public int getCoinValue() {
        return -5;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return false;
    }
}