package com.BluePrintHell.model.network;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.packets.Packet;
import javafx.geometry.Point2D;

public class ReferenceSystem extends NetworkSystem {
    public ReferenceSystem(String id, Point2D position) { super(id, position); }

    @Override
    public void receivePacket(Packet packet) {
        // If this is a destination reference system (has inputs, no outputs)
        if (!this.getInputPorts().isEmpty() && this.getOutputPorts().isEmpty()) {
            System.out.println("Packet successfully reached destination: " + this.id);
            this.getParentGameState().incrementPacketsSucceeded();
            // The packet is not added to the buffer, it just disappears.
        } else {
            // If it's a source reference system, just buffer it for launch
            super.receivePacket(packet);
        }
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

}