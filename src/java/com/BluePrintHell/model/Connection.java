package com.BluePrintHell.model;

import com.BluePrintHell.model.packets.Packet; // << این import را اضافه کنید
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Connection {
    private final Port startPort;
    private final Port endPort;
    private final List<Point2D> bendPoints = new ArrayList<>();

    // --- این بخش جدید است ---
    // این متغیر نگه می‌دارد که آیا پکتی در حال حاضر روی این سیم است یا نه
    private Packet packetOnWire = null;

    public Connection(Port startPort, Port endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
    }

    public double calculateLength() {
        // TODO: منطق محاسبه طول سیم (با در نظر گرفتن نقاط انحنا)
        return startPort.getPosition().distance(endPort.getPosition());
    }

    // ==========================================================
    // === این سه متد جدید را به کلاس خود اضافه کنید ===
    // ==========================================================
    /**
     * Checks if the wire is free (no packet is currently on it).
     * @return true if the wire is free, false otherwise.
     */
    public boolean isFree() {
        return packetOnWire == null;
    }

    /**
     * Marks the wire as occupied by a specific packet.
     * @param packet The packet that is now on this wire.
     */
    public void setPacketOnWire(Packet packet) {
        this.packetOnWire = packet;
    }

    /**
     * Marks the wire as free again. This is called when a packet reaches its destination.
     */
    public void clearPacket() {
        this.packetOnWire = null;
    }
    // ==========================================================

    // --- Getters ---
    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }
    public List<Point2D> getBendPoints() { return bendPoints; }
}