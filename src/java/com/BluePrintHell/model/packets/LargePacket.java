package com.BluePrintHell.model.packets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.geometry.Point2D;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "largePacketType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LargePacketTypeA.class, name = "LargePacketTypeA"),
        @JsonSubTypes.Type(value = LargePacketTypeB.class, name = "LargePacketTypeB")
})
public abstract class LargePacket extends Packet {
    private final UUID id = UUID.randomUUID();

    public LargePacket(Point2D startPosition, int size) {
        super(startPosition, size);
    }

    public UUID getId() {
        return id;
    }
}