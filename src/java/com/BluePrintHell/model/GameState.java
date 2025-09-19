package com.BluePrintHell.model;

import com.BluePrintHell.model.leveldata.SpawnEventData;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.packets.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private int levelNumber;
    private double playerWireLength;
    private int playerCoins;
    private double packetLoss;
    private double gameTime = 0;
    private int totalPacketsSpawned = 0;
    private int packetsLost = 0;
    private boolean isProgressiveMode = false;

    private List<NetworkSystem> systems = new ArrayList<>();
    private List<Packet> packets = new ArrayList<>();
    private List<Packet> packetsToRemove = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();
    private List<SpawnEventData> spawnEvents = new ArrayList<>();
    private int packetsSucceeded = 0;
    private final List<ActivePowerUp> activePowerUps = new CopyOnWriteArrayList<>();

    public GameState() {
    }

    public void incrementTotalPacketsSpawned() {
        this.totalPacketsSpawned++;
    }

    public boolean isConnectionFree(Connection connection) {
        if (connection == null) {
            return false;
        }
        for (Packet packet : this.packets) {
            if (packet.getCurrentConnection() == connection) {
                return false;
            }
        }
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
        if (packet != null && !packetsToRemove.contains(packet)) {
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

        for (ActivePowerUp powerUp : activePowerUps) {
            powerUp.update(deltaTime);
            if (powerUp.isFinished()) {
                activePowerUps.remove(powerUp);
            }
        }

        for (int i = packets.size() - 1; i >= 0; i--) {
            packets.get(i).update(deltaTime);
        }
        for (NetworkSystem system : systems) {
            system.update(deltaTime);
        }
    }

    public void addPowerUp(ActivePowerUp powerUp) {
        activePowerUps.removeIf(p -> p.getType() == powerUp.getType());
        activePowerUps.add(powerUp);
    }

    public boolean hasPowerUp(PowerUpType type) {
        return activePowerUps.stream().anyMatch(p -> p.getType() == type && !p.isFinished());
    }

    public boolean allSystemBuffersAreEmpty() {
        for (NetworkSystem system : systems) {
            if (!system.isBufferEmpty()) {
                return false;
            }
        }
        return true;
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

    public void enableProgressiveMode() {
        this.isProgressiveMode = true;
    }

    public boolean isProgressiveMode() {
        return isProgressiveMode;
    }

    public NetworkSystem getSourceSystem() {
        return systems.stream()
                .filter(s -> s instanceof com.BluePrintHell.model.network.ReferenceSystem)
                .filter(s -> s.getOutputPorts().size() > 0 && s.getInputPorts().size() == 0)
                .findFirst()
                .orElse(null);
    }

    public NetworkSystem getDestinationSystem() {
        return systems.stream()
                .filter(s -> s instanceof com.BluePrintHell.model.network.ReferenceSystem)
                .filter(s -> s.getInputPorts().size() > 0 && s.getOutputPorts().size() == 0)
                .findFirst()
                .orElse(null);
    }

    public List<NetworkSystem> getSystems() { return systems; }
    public void setSystems(List<NetworkSystem> systems) { this.systems = systems != null ? systems : new ArrayList<>(); }

    public List<Packet> getPackets() { return packets; }
    public void setPackets(List<Packet> packets) { this.packets = packets != null ? packets : new ArrayList<>(); }

    public List<Connection> getConnections() { return connections; }
    public void setConnections(List<Connection> connections) { this.connections = connections != null ? connections : new ArrayList<>(); }

    public List<SpawnEventData> getSpawnEvents() { return spawnEvents; }
    public void setSpawnEvents(List<SpawnEventData> spawnEvents) { this.spawnEvents = spawnEvents != null ? spawnEvents : new ArrayList<>(); }

    public double getGameTime() { return gameTime; }
    public void setGameTime(double gameTime) { this.gameTime = gameTime; }

    public int getPlayerCoins() { return playerCoins; }
    public void setPlayerCoins(int playerCoins) { this.playerCoins = playerCoins; }

    public int getTotalPacketsSpawned() { return totalPacketsSpawned; }
    public void setTotalPacketsSpawned(int totalPacketsSpawned) { this.totalPacketsSpawned = totalPacketsSpawned; }

    public int getPacketsLost() { return packetsLost; }
    public void setPacketsLost(int packetsLost) { this.packetsLost = packetsLost; }

    public void setPacketsSucceeded(int packetsSucceeded) { this.packetsSucceeded = packetsSucceeded; }

    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public void setPlayerWireLength(double playerWireLength) { this.playerWireLength = playerWireLength; }
    public void setProgressiveMode(boolean progressiveMode) { this.isProgressiveMode = progressiveMode; }
}