package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.packets.Packet;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpySystem extends NormalSystem {

    private static final Random random = new Random();

    public SpySystem(String id, Point2D position) {
        super(id, position);
    }

    public SpySystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        System.out.println("DEBUG: SpySystem " + this.id + " received packet " + packet.hashCode());
        super.receivePacket(packet);
    }

    @Override
    public void update(double deltaTime) {
        if (packetBuffer.isEmpty()) {
            return;
        }

        GameState gs = getParentGameState();
        if (gs == null) return;

        List<Port> availableExitPorts = gs.getSystems().stream()
                .filter(s -> s instanceof SpySystem)
                .flatMap(s -> s.getOutputPorts().stream())
                .filter(p -> p.isConnected() && gs.isConnectionFree(p.getAttachedConnection()))
                .collect(Collectors.toList());

        if (availableExitPorts.isEmpty()) {
            System.out.println("DEBUG: No free exit port found in the entire Spy Network. Waiting...");
            return;
        }

        Packet packetToLaunch = packetBuffer.poll();
        Port targetPort = availableExitPorts.get(random.nextInt(availableExitPorts.size()));

        System.out.println("DEBUG: Spy network routing packet " + packetToLaunch.hashCode() + " to exit port " + targetPort.getId() + " on system " + targetPort.getParentSystem().getId());

        Point2D spawnPosition = targetPort.getCenterPosition();
        packetToLaunch.setPosition(spawnPosition);
        packetToLaunch.launch(targetPort.getAttachedConnection());
        gs.addPacket(packetToLaunch);
    }
}