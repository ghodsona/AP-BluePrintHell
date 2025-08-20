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

    // --- متغیرهای وضعیت کلی مرحله ---
    private int levelNumber;
    private double playerWireLength;
    private int playerCoins;
    private double packetLoss;
    private double gameTime = 0; // زمان سپری شده از شروع فاز اجرا

    // --- لیست‌های موجودیت‌های زنده در بازی ---
    private final List<NetworkSystem> systems = new ArrayList<>();
    private final List<Packet> packets = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private List<SpawnEventData> spawnEvents = new ArrayList<>(); // رویدادهای تولید پکت

    /**
     * این متد اصلی، وضعیت کل بازی را یک فریم به جلو می‌برد.
     * این متد باید در هر فریم از حلقه بازی (AnimationTimer) فراخوانی شود.
     * @param deltaTime زمان سپری شده از فریم قبلی (به ثانیه)
     */
    public void update(double deltaTime) {
        // ۱. زمان کلی بازی را افزایش بده
        this.gameTime += deltaTime;

        // ۲. وضعیت تمام پکت‌های فعال را آپدیت کن
        for (Packet packet : packets) {
            packet.update(deltaTime);
        }

        // ۳. وضعیت تمام سیستم‌های فعال را آپدیت کن
        for (NetworkSystem system : systems) {
            system.update(deltaTime);
        }
    }

    // --- متدهای مدیریتی برای لیست‌ها ---

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

    public void resetGameTime() {
        this.gameTime = 0;
    }

    public List<NetworkSystem> getSystems() { return systems; }
    public List<Packet> getPackets() { return packets; }
    public List<Connection> getConnections() { return connections; }
    public List<SpawnEventData> getSpawnEvents() { return spawnEvents; }
    public double getGameTime() { return gameTime; }

    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public void setPlayerWireLength(double playerWireLength) { this.playerWireLength = playerWireLength; }
    public void setPlayerCoins(int playerCoins) { this.playerCoins = playerCoins; }
    public void setSpawnEvents(List<SpawnEventData> spawnEvents) { this.spawnEvents = spawnEvents; }
}