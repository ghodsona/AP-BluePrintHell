package com.BluePrintHell.util;

import com.BluePrintHell.model.packets.Packet;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionManager {
    private final Map<String, List<Packet>> grid;
    private final int cellSize;

    public CollisionManager(int cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
    }

    public void checkCollisions(List<Packet> allPackets) {
        // Broad Phase: Populate the grid
        populateGrid(allPackets);

        // Narrow Phase: Check for collisions only within cells
        for (List<Packet> cellPackets : grid.values()) {
            if (cellPackets.size() < 2) {
                continue; // No collision possible with less than 2 packets
            }

            for (int i = 0; i < cellPackets.size(); i++) {
                for (int j = i + 1; j < cellPackets.size(); j++) {
                    Packet p1 = cellPackets.get(i);
                    Packet p2 = cellPackets.get(j);
                    if (arePacketsColliding(p1, p2)) {
                        resolveCollision(p1, p2, allPackets);
                    }
                }
            }
        }
    }

    private void populateGrid(List<Packet> packets) {
        grid.clear();
        for (Packet packet : packets) {
            int cellX = (int) (packet.getPosition().getX() / cellSize);
            int cellY = (int) (packet.getPosition().getY() / cellSize);
            String key = cellX + "," + cellY;

            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(packet);
        }
    }

    private boolean arePacketsColliding(Packet p1, Packet p2) {
        // Simple circular collision check
        double radius1 = p1.getSize() / 2.0;
        double radius2 = p2.getSize() / 2.0;
        double distance = p1.getPosition().distance(p2.getPosition());
        return distance < (radius1 + radius2);
    }

    private void resolveCollision(Packet p1, Packet p2, List<Packet> allPackets) {
        System.out.println("Collision detected between two packets!");
        // TODO: Add noise to p1 and p2
        // p1.addNoise(NOISE_AMOUNT);
        // p2.addNoise(NOISE_AMOUNT);

        // --- Impact Wave Logic ---
        Point2D collisionPoint = p1.getPosition().midpoint(p2.getPosition());
        double impactRadius = 150; // Max radius of the impact wave
        double impactForce = 50;   // Max force of the impact

        for (Packet otherPacket : allPackets) {
            if (otherPacket == p1 || otherPacket == p2) continue;

            double distance = otherPacket.getPosition().distance(collisionPoint);
            if (distance < impactRadius) {
                // The closer the packet, the stronger the push
                double forceFalloff = 1 - (distance / impactRadius);
                Point2D pushDirection = otherPacket.getPosition().subtract(collisionPoint).normalize();
                Point2D pushVector = pushDirection.multiply(impactForce * forceFalloff);

                // Apply the impact by modifying the packet's velocity
                otherPacket.applyForce(pushVector);
            }
        }
    }
}