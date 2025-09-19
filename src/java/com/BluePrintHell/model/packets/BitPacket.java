package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;
import java.util.UUID;

// بیت‌پکت‌ها از نوع CirclePacket با اندازه ۱ هستند
public class BitPacket extends CirclePacket {
    private UUID parentLargePacketId;

    public BitPacket(Point2D startPosition, UUID parentId) {
        super(startPosition);
        this.parentLargePacketId = parentId;
    }

    public BitPacket() { }

    public UUID getParentLargePacketId() {
        return parentLargePacketId;
    }
}