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
        if (packetBuffer.isEmpty() || wasJustArrived(packetBuffer.peek())) {
            return;
        }

        GameState gs = getParentGameState();
        if (gs == null) return;

        // 1. Find all available exit ports across the entire spy network
        List<Port> availableExitPorts = gs.getSystems().stream()
                .filter(s -> s instanceof SpySystem)
                .flatMap(s -> s.getOutputPorts().stream())
                .filter(p -> p.isConnected() && gs.isConnectionFree(p.getAttachedConnection()))
                .collect(Collectors.toList());

        // 2. If no ports are free, wait until the next frame. Do NOT remove the packet.
        if (availableExitPorts.isEmpty()) {
            System.out.println("DEBUG: No free exit port found in the entire Spy Network. Waiting...");
            return;
        }

        // 3. A free port is available, so now we can safely remove the packet from the buffer.
        Packet packetToLaunch = packetBuffer.poll(); // Changed from peek() to poll() here
        Port targetPort = availableExitPorts.get(random.nextInt(availableExitPorts.size()));

        System.out.println("DEBUG: Spy network routing packet " + packetToLaunch.hashCode() + " to exit port " + targetPort.getId() + " on system " + targetPort.getParentSystem().getId());

        // 4. Launch the packet from the chosen exit port.
        Point2D spawnPosition = targetPort.getCenterPosition();
        packetToLaunch.setPosition(spawnPosition);
        packetToLaunch.launch(targetPort.getAttachedConnection());
        gs.addPacket(packetToLaunch);
    }
}