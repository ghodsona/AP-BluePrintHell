package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.packets.Packet;
import com.BluePrintHell.model.packets.SquarePacket;
import com.BluePrintHell.model.packets.TrojanPacket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.geometry.Point2D;

// تغییر: به جای NetworkSystem از NormalSystem ارث‌بری می‌کند
public class AntiTrojanSystem extends NormalSystem {

    private static final double SCAN_RADIUS = 150.0; // شعاع اسکن سیستم
    private static final double COOLDOWN_TIME = 5.0; // 5 ثانیه زمان غیرفعال بودن
    private double currentCooldown = 0;

    public AntiTrojanSystem(String id, Point2D position) {
        super(id, position);
    }

    public AntiTrojanSystem() {
        super(null, null);
    }

    public boolean isActive() {
        return currentCooldown <= 0;
    }

    @Override
    public void update(double deltaTime) {
        // ابتدا بافر ورودی را پردازش کرده و پکت‌ها را مانند یک سیستم عادی پرتاب می‌کند
        super.update(deltaTime); // این فراخوانی حالا صحیح است

        // اگر سیستم در حالت Cooldown باشد، زمان آن را کم کرده و خارج می‌شود
        if (!isActive()) {
            currentCooldown -= deltaTime;
            return;
        }

        GameState gs = getParentGameState();
        if (gs == null) return;

        // اسکن محیط اطراف برای پیدا کردن پکت‌های تروجان
        for (Packet packet : gs.getPackets()) {
            if (packet instanceof TrojanPacket) {
                double distance = this.getCenterPosition().distance(packet.getVisualPosition());

                if (distance <= SCAN_RADIUS) {
                    System.out.println("Trojan packet neutralized!");

                    // پکت تروجان را با یک پکت عادی جایگزین می‌کند
                    neutralizeTrojan((TrojanPacket) packet);

                    // سیستم را وارد حالت Cooldown می‌کند
                    this.currentCooldown = COOLDOWN_TIME;

                    // در هر فریم فقط یک تروجان خنثی می‌شود
                    break;
                }
            }
        }
    }

    private void neutralizeTrojan(TrojanPacket trojanPacket) {
        GameState gs = getParentGameState();
        if (gs == null) return;
        SquarePacket newPacket = new SquarePacket(trojanPacket.getVisualPosition());

        if (trojanPacket.getCurrentConnection() != null) {
            newPacket.takeOverPath(trojanPacket);
        }

        gs.addPacket(newPacket);
        gs.removePacket(trojanPacket);
    }

    @JsonIgnore
    public Point2D getCenterPosition() {
        if (position == null) return Point2D.ZERO;
        double SYSTEM_WIDTH = 120;
        double SYSTEM_HEIGHT = 80;
        return new Point2D(position.getX() + SYSTEM_WIDTH / 2, position.getY() + SYSTEM_HEIGHT / 2);
    }
}