package com.BluePrintHell.model.leveldata;

public class SpawnEventData {
    private double spawnTime; // in seconds from the start of the level
    private String sourceSystemId;
    private String packetType; // e.g., "SQUARE_MESSENGER"

    // Getters and Setters
    public double getSpawnTime() { return spawnTime; }
    public void setSpawnTime(double spawnTime) { this.spawnTime = spawnTime; }
    public String getSourceSystemId() { return sourceSystemId; }
    public void setSourceSystemId(String sourceSystemId) { this.sourceSystemId = sourceSystemId; }
    public String getPacketType() { return packetType; }
    public void setPacketType(String packetType) { this.packetType = packetType; }
}