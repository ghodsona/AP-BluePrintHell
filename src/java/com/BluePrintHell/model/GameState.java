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
    private final List<Packet> packetsToRemove = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private List<SpawnEventData> spawnEvents = new ArrayList<>(); // رویدادهای تولید پکت
    private int packetsSucceeded = 0;

    public void incrementTotalPacketsSpawned() {
        this.totalPacketsSpawned++;
    }

    public boolean isConnectionFree(Connection connection) {
        if (connection == null) {
            return false; // اتصال نامعتبر آزاد نیست
        }
        for (Packet packet : this.packets) {
            // از متد کمکی که به کلاس Packet اضافه می‌کنیم استفاده می‌کند
            if (packet.getCurrentConnection() == connection) {
                // یک پکت روی این اتصال پیدا شد، پس آزاد نیست
                return false;
            }
        }
        // هیچ پکتی روی این اتصال پیدا نشد، پس آزاد است
        return true;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public double getPlayerWireLength() {return playerWireLength; }

    public void removePacket(Packet packet) {
        if (packet != null) {
            packetsToRemove.add(packet);
        }
    }

    public void incrementPacketsSucceeded() {
        this.packetsSucceeded++;
    }

    public void cleanupLists() {
        packets.removeAll(packetsToRemove);
        packetsToRemove.clear();
        for (NetworkSystem system : systems) {
            system.clearJustArrived();
        }
    }

    public void losePacket(Packet packet) {
        if (packet != null && !packetsToRemove.contains(packet)) { // برای جلوگیری از شمارش چندباره
            System.out.println("PACKET LOST: A packet was lost due to excessive noise or other reasons.");
            this.packetsLost++;
            removePacket(packet);
        }
    }

    public int getPacketsSucceeded() { return packetsSucceeded; }

    public void incrementPacketsLost() {
        this.packetsLost++;
    }

    public void update(double deltaTime) {
        this.gameTime += deltaTime;
        for (int i = packets.size() - 1; i >= 0; i--) {
            packets.get(i).update(deltaTime);
        }
        for (NetworkSystem system : systems) {
            system.update(deltaTime);
        }
    }

    public boolean allSystemBuffersAreEmpty() {
        for (NetworkSystem system : systems) {
            if (!system.isBufferEmpty()) {
                return false; // Found a system with a packet still in its buffer
            }
        }
        return true; // All systems are clear
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