package com.BluePrintHell.model;

import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.packets.Packet;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private int levelNumber;
    private double playerWireLength;
    private int playerCoins;
    private double packetLoss;

    private final List<NetworkSystem> systems = new ArrayList<>();
    private final List<Packet> packets = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();

    public void addSystem(NetworkSystem system) {
        systems.add(system);
    }

    public void addPacket(Packet packet) {
        packets.add(packet);
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    public void removeConnection(Connection connection) {
        if (connection != null) {
            connections.remove(connection);
        }
    }

    // TODO: Add methods to update the game state each frame
    public void update(double deltaTime) {
        for (Packet packet : packets) {
            packet.update(deltaTime);
        }
        for (NetworkSystem system : systems) {
            system.update(deltaTime);
        }
    }

    // --- Getters for rendering ---
    public List<NetworkSystem> getSystems() { return systems; }
    public List<Packet> getPackets() { return packets; }
    public List<Connection> getConnections() { return connections; }

    // --- Getters and Setters for HUD ---
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public void setPlayerWireLength(double playerWireLength) { this.playerWireLength = playerWireLength; }
    public void setPlayerCoins(int playerCoins) { this.playerCoins = playerCoins; }
}