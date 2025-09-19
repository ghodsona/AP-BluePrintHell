package com.BluePrintHell.model;

import com.BluePrintHell.model.packets.LargePacket;

public record LargePacketInfo(Class<? extends LargePacket> packetClass, int size) {
}