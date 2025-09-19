package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.packets.ProtectedPacket;
// import com.BluePrintHell.model.packets.ConfidentialPacket;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpySystem extends NormalSystem { // می‌تواند از NormalSystem ارث‌بری کند

    private final Random random = new Random();

    public SpySystem(String id, Point2D position) {
        super(id, position);
    }

    public SpySystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        GameState gs = getParentGameState();
        if (gs == null) {
            super.receivePacket(packet); // بازگشت به رفتار عادی در صورت نبودن GameState
            return;
        }

        // پکت‌های محافظت‌شده تحت تاثیر قرار نمی‌گیرند
        if (packet instanceof ProtectedPacket) {
            super.receivePacket(packet);
            return;
        }

        // TODO: در آینده که پکت‌های محرمانه اضافه شدند، این بخش را کامل کنید
        // if (packet instanceof ConfidentialPacket) {
        //     System.out.println("Confidential packet destroyed by SpySystem!");
        //     getParentGameState().losePacket(packet);
        //     return;
        // }

        // پیدا کردن تمام سیستم‌های جاسوسی دیگر در شبکه
        List<SpySystem> otherSpySystems = gs.getSystems().stream()
                .filter(s -> s instanceof SpySystem && s != this)
                .map(s -> (SpySystem) s)
                .collect(Collectors.toList());

        if (!otherSpySystems.isEmpty()) {
            SpySystem targetSystem = otherSpySystems.get(random.nextInt(otherSpySystems.size()));
            System.out.println("Packet teleported from " + getId() + " to " + targetSystem.getId());
            targetSystem.receivePacket(packet);
        } else {
            super.receivePacket(packet);
        }
    }
}