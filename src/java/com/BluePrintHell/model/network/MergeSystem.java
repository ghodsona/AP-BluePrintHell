package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.LargePacketInfo;
import com.BluePrintHell.model.packets.BitPacket;
import com.BluePrintHell.model.packets.LargePacket;
import com.BluePrintHell.model.packets.Packet;
import com.fasterxml.jackson.annotation.JsonIgnore; // <-- ایمپورت جدید
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MergeSystem extends NormalSystem {

    private final Map<UUID, List<BitPacket>> collectedBitPackets = new HashMap<>();

    public MergeSystem(String id, Point2D position) {
        super(id, position);
    }

    public MergeSystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        if (packet instanceof BitPacket) {
            BitPacket bitPacket = (BitPacket) packet;
            UUID parentId = bitPacket.getParentLargePacketId();

            collectedBitPackets.computeIfAbsent(parentId, k -> new ArrayList<>()).add(bitPacket);
            System.out.println("MergeSystem collected a BitPacket for ID: " + parentId);

            checkForCompletion(parentId);
        } else {
            super.receivePacket(packet);
        }
    }

    private void checkForCompletion(UUID parentId) {
        GameState gs = getParentGameState();
        if (gs == null) return;

        LargePacketInfo info = gs.getLargePacketInfo(parentId);
        if (info == null) {
            System.err.println("Error: No info found for LargePacket ID: " + parentId);
            return;
        }

        List<BitPacket> bits = collectedBitPackets.get(parentId);
        if (bits != null && bits.size() >= info.size()) {
            System.out.println("All BitPackets collected for " + parentId + "! Reconstructing...");

            try {
                // این خط اکنون به درستی کار می‌کند
                LargePacket reconstructedPacket = info.packetClass()
                        .getConstructor(Point2D.class)
                        .newInstance(this.getCenterPosition());

                packetBuffer.add(reconstructedPacket);
                collectedBitPackets.remove(parentId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // متد کمکی برای گرفتن مرکز سیستم
    @JsonIgnore
    public Point2D getCenterPosition() {
        if (position == null) return Point2D.ZERO;
        double SYSTEM_WIDTH = 120;
        double SYSTEM_HEIGHT = 80;
        return new Point2D(position.getX() + SYSTEM_WIDTH / 2, position.getY() + SYSTEM_HEIGHT / 2);
    }
}