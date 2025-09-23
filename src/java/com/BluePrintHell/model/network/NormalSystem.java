package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.Port;
import javafx.geometry.Point2D;

public class NormalSystem extends NetworkSystem {
    public NormalSystem(String id, Point2D position) {
        super(id, position);
    }
    public NormalSystem() {
        super(null, null);
    }

    @Override
    public void update(double deltaTime) {
        if (packetBuffer.isEmpty() || wasJustArrived(packetBuffer.peek())) {
            return;
        }

        Packet packetToLaunch = packetBuffer.peek();
        Port targetPort = findOutputPortFor(packetToLaunch);

        if (targetPort != null) {
            boolean isSpawnAreaClear = true;
            Point2D spawnPosition = targetPort.getCenterPosition();

            for (Packet existingPacket : this.getParentGameState().getPackets()) {
                if (existingPacket.getVisualPosition().distance(spawnPosition) < 20) { // 20 is a safe radius
                    isSpawnAreaClear = false;
                    break;
                }
            }

            if (isSpawnAreaClear) {
                packetBuffer.poll();
                System.out.println("DEBUG: System " + this.getId() + " is launching packet " + packetToLaunch.hashCode() + " from port " + targetPort.getId());
                packetToLaunch.setPosition(spawnPosition);
                packetToLaunch.launch(targetPort.getAttachedConnection());
                this.getParentGameState().addPacket(packetToLaunch);
            }
        }
    }

    /**
     * Finds a suitable output port for a given packet based on game rules.
     * Rule: Priority 1: Compatible & Free. Priority 2: Any Free port.
     * @param packet The packet to be routed.
     * @return A suitable Port, or null if none is available.
     */
    private Port findOutputPortFor(Packet packet) {
        GameState gs = getParentGameState();
        if (gs == null) return null;

        // Priority 1: Find a connected, free, and COMPATIBLE port
        for (Port port : outputPorts) {
            // ✅✅✅ اصلاحیه اصلی اینجاست ✅✅✅
            if (port.isConnected() && gs.isConnectionFree(port.getAttachedConnection()) &&
                    packet.isCompatibleWith(port.getShape())) {
                return port; // Found the best possible port
            }
        }

        // Priority 2: If no compatible port was found, find ANY connected, FREE port
        for (Port port : outputPorts) {
            // ✅✅✅ اصلاحیه اصلی اینجاست ✅✅✅
            if (port.isConnected() && gs.isConnectionFree(port.getAttachedConnection())) {
                return port; // Found a fallback port
            }
        }

        return null; // No available port found at this moment
    }

}