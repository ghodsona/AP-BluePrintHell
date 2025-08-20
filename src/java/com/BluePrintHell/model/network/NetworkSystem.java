package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class NetworkSystem {
    protected final String id;
    protected Point2D position;
    protected List<Port> inputPorts = new ArrayList<>();
    protected List<Port> outputPorts = new ArrayList<>();
    protected Queue<Packet> packetBuffer = new LinkedList<>(); // بافر برای پکت‌های منتظر
    protected GameState parentGameState; // ارجاع به وضعیت کلی بازی

    public void setParentGameState(GameState parentGameState) {
        this.parentGameState = parentGameState;
    }

    public GameState getParentGameState() {
        return parentGameState;
    }

    public NetworkSystem(String id, Point2D position) {
        this.id = id;
        this.position = position;
    }

    // متدی برای دریافت یک پکت جدید
    public void receivePacket(Packet packet) {
        packetBuffer.add(packet);
        // روشن کردن اندیکاتور سیستم
    }

    // هر نوع سیستم باید منطق پردازش پکت خود را پیاده‌سازی کند
    public abstract void update(double deltaTime);

    // --- Getters ---
    public String getId() { return id; }
    public Point2D getPosition() { return position; }
    public List<Port> getInputPorts() { return inputPorts; }
    public List<Port> getOutputPorts() { return outputPorts; }

    public void setPosition(Point2D position) {
        this.position = position;
    }
}