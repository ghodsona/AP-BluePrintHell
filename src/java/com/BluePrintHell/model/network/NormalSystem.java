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
        // Do nothing if buffer is empty
        if (packetBuffer.isEmpty()) {
            return;
        }

        Packet packetToLaunch = packetBuffer.peek(); // Look at the next packet without removing it
        Port targetPort = findOutputPortFor(packetToLaunch);

        // If a valid output port was found
        if (targetPort != null) {
            packetBuffer.poll(); // Now remove the packet from the buffer

            // Set packet's initial position to the center of the output port
            packetToLaunch.setPosition(new Point2D(
                    targetPort.getPosition().getX() + 6, // PORT_SIZE / 2
                    targetPort.getPosition().getY() + 6
            ));

            // Launch the packet onto the wire
            packetToLaunch.launch(targetPort.getAttachedConnection());

            // Add the packet to the main list of moving packets
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