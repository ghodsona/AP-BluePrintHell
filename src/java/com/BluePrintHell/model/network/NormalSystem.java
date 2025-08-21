package com.BluePrintHell.model.network;

import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.stream.Collectors;

public class NormalSystem extends NetworkSystem {
    public NormalSystem(String id, Point2D position) {
        super(id, position);
    }

    @Override
    public void update(double deltaTime) {
        if (packetBuffer.isEmpty()) {
            return; // Nothing to do if buffer is empty
        }

        Packet packetToLaunch = packetBuffer.peek(); // Look at the next packet without removing
        Port targetPort = null;

        // Priority 1: Find a connected and COMPATIBLE port
        for (Port port : outputPorts) {
            if (port.isConnected() && packetToLaunch.isCompatibleWith(port.getShape())) {
                // TODO: Also check if the wire is free
                targetPort = port;
                break; // Found the best possible port, no need to search further
            }
        }

        // Priority 2: If no compatible port was found, find ANY connected port
        if (targetPort == null) {
            for (Port port : outputPorts) {
                if (port.isConnected()) {
                    // TODO: Also check if the wire is free
                    targetPort = port;
                    break; // Found a fallback port
                }
            }
        }

        // If a suitable port was found (either priority 1 or 2)
        if (targetPort != null) {
            packetBuffer.poll(); // Now, officially remove the packet from the buffer

            packetToLaunch.setPosition(new Point2D(
                    targetPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    targetPort.getPosition().getY() + 6
            ));

            packetToLaunch.launch(targetPort.getAttachedConnection());
            this.getParentGameState().addPacket(packetToLaunch);
        }
    }

    /**
     * Finds a suitable output port for a given packet based on game rules.
     * Rule: Priority 1: Compatible & Free. Priority 2: Any Free port.
     * @param packet The packet to be routed.
     * @return A suitable Port, or null if none is available.
     */
    private Port findOutputPortFor(Packet packet) {
        // Priority 1: Find a connected port with a compatible shape where the wire is free
        for (Port port : outputPorts) {
            if (port.isConnected() && packet.isCompatibleWith(port.getShape())) {
                // TODO: Check if the connection is free (no other packet on it)
                return port;
            }
        }

        // Priority 2: Find any connected port where the wire is free
        for (Port port : outputPorts) {
            if (port.isConnected()) {
                // TODO: Check if the connection is free
                return port;
            }
        }

        return null; // No available port found
    }
}