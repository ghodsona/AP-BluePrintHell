package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.packets.ProtectedPacket;
import com.BluePrintHell.model.packets.TrojanPacket;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SaboteurSystem extends NetworkSystem {
    private static final double TROJAN_CONVERSION_CHANCE = 1;
    private final Random random = new Random();

    public SaboteurSystem(String id, Point2D position) {
        super(id, position);
    }

    public SaboteurSystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        if (packet instanceof ProtectedPacket) {
            super.receivePacket(packet);
            return;
        }

        if (packet.getNoise() == 0) {
            packet.addNoise(1);
        }

        if (random.nextDouble() < 1.0) { // Using 1.0 for testing as requested
            System.out.println("Packet converted to Trojan!");
            TrojanPacket trojanPacket = new TrojanPacket(packet);
            super.receivePacket(trojanPacket);
        } else {
            super.receivePacket(packet);
        }
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
                if (existingPacket.getVisualPosition().distance(spawnPosition) < 20) {
                    isSpawnAreaClear = false;
                    break;
                }
            }

            if (isSpawnAreaClear) {
                packetBuffer.poll(); // Remove packet from buffer
                System.out.println("DEBUG: SaboteurSystem " + this.getId() + " is launching packet " + packetToLaunch.hashCode() + " from port " + targetPort.getId());
                packetToLaunch.setPosition(spawnPosition);
                packetToLaunch.launch(targetPort.getAttachedConnection());
                this.getParentGameState().addPacket(packetToLaunch);
            }
        }
    }

    private Port findOutputPortFor(Packet packet) {
        GameState gs = getParentGameState();
        if (gs == null) return null;

        List<Port> availablePorts = new ArrayList<>();
        for (Port port : outputPorts) {
            if (port.isConnected() && gs.isConnectionFree(port.getAttachedConnection())) {
                availablePorts.add(port);
            }
        }

        if (availablePorts.isEmpty()) {
            return null; // No free ports at all
        }

        // Sabotage logic: First, try to find an INCOMPATIBLE port
        List<Port> incompatiblePorts = new ArrayList<>();
        for (Port port : availablePorts) {
            if (!packet.isCompatibleWith(port.getShape())) {
                incompatiblePorts.add(port);
            }
        }

        if (!incompatiblePorts.isEmpty()) {
            Collections.shuffle(incompatiblePorts);
            return incompatiblePorts.get(0); // Prioritize sending to a wrong port
        } else {
            // If no incompatible ports are free, send to any free port
            Collections.shuffle(availablePorts);
            return availablePorts.get(0);
        }
    }
}