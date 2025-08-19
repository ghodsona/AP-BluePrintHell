package com.BluePrintHell.model.leveldata;

import java.util.List;

public class LevelData {
    private int levelNumber;
    private String levelName;
    private double initialWireLength;
    private int initialCoins;
    private List<SystemData> systems;
    private List<SpawnEventData> spawnEvents;

    // Getters and Setters
    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public double getInitialWireLength() { return initialWireLength; }
    public void setInitialWireLength(double initialWireLength) { this.initialWireLength = initialWireLength; }
    public int getInitialCoins() { return initialCoins; }
    public void setInitialCoins(int initialCoins) { this.initialCoins = initialCoins; }
    public List<SystemData> getSystems() { return systems; }
    public void setSystems(List<SystemData> systems) { this.systems = systems; }
    public List<SpawnEventData> getSpawnEvents() { return spawnEvents; }
    public void setSpawnEvents(List<SpawnEventData> spawnEvents) { this.spawnEvents = spawnEvents; }
}