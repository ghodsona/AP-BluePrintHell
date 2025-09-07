package com.BluePrintHell.model.network;

import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import javafx.geometry.Point2D;

public class NormalSystem extends NetworkSystem {
    public NormalSystem(String id, Point2D position) { super(id, position); }

    // In both NormalSystem.java and ReferenceSystem.java
    @Override
    public void update(double deltaTime) {
        if (packetBuffer.isEmpty() || wasJustArrived(packetBuffer.peek())) {
            return;
        }

        Packet packetToLaunch = packetBuffer.peek();
        Port targetPort = findOutputPortFor(packetToLaunch);

        if (targetPort != null) {
            // Check if the spawn area in front of the port is clear
            boolean isSpawnAreaClear = true;
            Point2D spawnPosition = new Point2D(
                    targetPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    targetPort.getPosition().getY() + 6
            );

            for (Packet existingPacket : this.getParentGameState().getPackets()) {
                if (existingPacket.getVisualPosition().distance(spawnPosition) < 20) { // 20 is a safe radius
                    isSpawnAreaClear = false;
                    break;
                }
            }

            // Only launch if the area is clear
            if (isSpawnAreaClear) {
                packetBuffer.poll(); // Remove packet from buffer

                packetToLaunch.setPosition(spawnPosition);
                packetToLaunch.launch(targetPort.getAttachedConnection());
                this.getParentGameState().addPacket(packetToLaunch);
            }
        }
    }

    // In both NormalSystem.java and ReferenceSystem.java

    /**
     * Finds a suitable output port for a given packet based on game rules.
     * Rule: Priority 1: Compatible & Free. Priority 2: Any Free port.
     * @param packet The packet to be routed.
     * @return A suitable Port, or null if none is available.
     */
    private Port findOutputPortFor(Packet packet) {
        // Priority 1: Find a connected, free, and COMPATIBLE port
        for (Port port : outputPorts) {
            if (port.isConnected() && port.getAttachedConnection().isFree() &&
                    packet.isCompatibleWith(port.getShape())) {
                return port; // Found the best possible port
            }
        }

        // Priority 2: If no compatible port was found, find ANY connected, FREE port
        for (Port port : outputPorts) {
            if (  port.isConnected() && port.getAttachedConnection().isFree()) {
                return port; // Found a fallback port
            }
        }

        return null; // No available port found at this moment
    }

}