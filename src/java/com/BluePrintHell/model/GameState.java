package com.BluePrintHell.model;

import com.BluePrintHell.model.leveldata.SpawnEventData;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.packets.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * این کلاس وضعیت کامل و زنده یک مرحله از بازی را در هر لحظه نگه می‌دارد.
 * تمام آبجکت‌های فعال، اطلاعات بازیکن و زمان بازی در اینجا مدیریت می‌شوند.
 */
public class GameState {
    private int levelNumber;
    private double playerWireLength;
    private int playerCoins;
    private double packetLoss;
    private double gameTime = 0;
    private int totalPacketsSpawned = 0;
    private int packetsLost = 0;

    private final List<NetworkSystem> systems = new ArrayList<>();
    private final List<Packet> packets = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private List<SpawnEventData> spawnEvents = new ArrayList<>(); // رویدادهای تولید پکت
    private int packetsSucceeded = 0;

    public void incrementTotalPacketsSpawned() {
        this.totalPacketsSpawned++;
    }

    public void removePacket(Packet packet) {
        if (packet != null) {
            // Use iterator to safely remove while iterating
            packets.removeIf(p -> p.equals(packet));
        }
    }

    public void incrementPacketsSucceeded() {
        this.packetsSucceeded++;
    }

    public int getPacketsSucceeded() { return packetsSucceeded; }

    public void incrementPacketsLost() {
        this.packetsLost++;
    }

    public void update(double deltaTime) {
        this.gameTime += deltaTime;
        for (Packet packet : packets) {
            packet.update(deltaTime);
        }
        for (NetworkSystem system : systems) {
            system.update(deltaTime);
        }
    }

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

    public void addCoins(int amount) {
        this.playerCoins += amount;
    }

    public void resetGameTime() {
        this.gameTime = 0;
    }

    public List<NetworkSystem> getSystems() { return systems; }
    public List<Packet> getPackets() { return packets; }
    public List<Connection> getConnections() { return connections; }
    public List<SpawnEventData> getSpawnEvents() { return spawnEvents; }
    public double getGameTime() { return gameTime; }
    public int getPlayerCoins() { return playerCoins; }
    public int getTotalPacketsSpawned() { return totalPacketsSpawned; }
    public int getPacketsLost() { return packetsLost; }

    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public void setPlayerWireLength(double playerWireLength) { this.playerWireLength = playerWireLength; }
    public void setPlayerCoins(int playerCoins) { this.playerCoins = playerCoins; }
    public void setSpawnEvents(List<SpawnEventData> spawnEvents) { this.spawnEvents = spawnEvents; }
}