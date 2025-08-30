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
            return; // Buffer is empty, nothing to do.
        }

        System.out.println("DEBUG: System " + getId() + " has " + packetBuffer.size() + " packet(s) in buffer. Trying to launch one.");

        Packet packetToLaunch = packetBuffer.peek();

        if (wasJustArrived(packetToLaunch)) {
            return;
        }

        Port targetPort = findOutputPortFor(packetToLaunch);

        if (targetPort != null) {
            System.out.println(" -> SUCCESS: Found valid output port: " + targetPort.getId());

            packetBuffer.poll(); // Remove the packet from the buffer

            packetToLaunch.setPosition(new Point2D(
                    targetPort.getPosition().getX() + 6,
                    targetPort.getPosition().getY() + 6
            ));

            packetToLaunch.launch(targetPort.getAttachedConnection());
            this.getParentGameState().addPacket(packetToLaunch);

        } else {
            // --- این پیام دیباگ بسیار مهم است ---
            System.out.println(" -> FAILED: No available output port found for packet. Packet remains in buffer.");
        }
    }

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