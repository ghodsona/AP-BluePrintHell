package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class ProtectedConfidentialPacket extends Packet {
    private static final double BASE_SPEED = 90;
    private static final double TARGET_SEPARATION = 50.0;
    private static final double REPOSITION_FORCE = 5.0;
    private static final int COIN_VALUE = 4;
    private static final int PACKET_SIZE = 6;

    public ProtectedConfidentialPacket(Point2D startPosition) {
        super(startPosition, PACKET_SIZE);
        this.currentSpeed = BASE_SPEED;
    }

    public ProtectedConfidentialPacket() {
        super(null, PACKET_SIZE);
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        applySeparationForce();
    }

    private void applySeparationForce() {
        GameState gs = getParentGameState();
        if (gs == null || getVisualPosition() == null) return;

        Point2D totalForce = Point2D.ZERO;

        for (Packet other : gs.getPackets()) {
            if (other == this || other.getVisualPosition() == null) continue;

            double distance = this.getVisualPosition().distance(other.getVisualPosition());

            if (distance < TARGET_SEPARATION) {
                Point2D direction = this.getVisualPosition().subtract(other.getVisualPosition()).normalize();
                double forceMagnitude = REPOSITION_FORCE * (1 - (distance / TARGET_SEPARATION));

                totalForce = totalForce.add(direction.multiply(forceMagnitude));
            }
        }

        if (!totalForce.equals(Point2D.ZERO)) {
            applyForce(totalForce);
        }
    }

    @Override
    public int getCoinValue() {
        return COIN_VALUE;
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        return false;
    }
}