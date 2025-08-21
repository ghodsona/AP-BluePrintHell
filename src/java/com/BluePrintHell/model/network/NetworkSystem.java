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

    // Use a capacity constant
    protected static final int BUFFER_CAPACITY = 5;
    protected Queue<Packet> packetBuffer = new LinkedList<>();

    protected GameState parentGameState;

    public NetworkSystem(String id, Point2D position) {
        this.id = id;
        this.position = position;
    }

    // Update the receivePacket method to check capacity
    public void receivePacket(Packet packet) {
        if (packetBuffer.size() < BUFFER_CAPACITY) {
            packetBuffer.add(packet);
            if (this instanceof NormalSystem || (this instanceof ReferenceSystem && !this.getInputPorts().isEmpty())) {
                this.getParentGameState().addCoins(packet.getCoinValue());
            }
        } else {
            this.getParentGameState().incrementPacketsLost();
            System.err.println("System " + id + " buffer is full. Packet lost.");
            // TODO: Update packetLoss in GameState
        }
    }

    public abstract void update(double deltaTime);

    public void setParentGameState(GameState parentGameState) {
        this.parentGameState = parentGameState;
    }

    public GameState getParentGameState() {
        return parentGameState;
    }

    // --- Getters ---
    public String getId() { return id; }
    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }
    public List<Port> getInputPorts() { return inputPorts; }
    public List<Port> getOutputPorts() { return outputPorts; }
}