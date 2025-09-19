package com.BluePrintHell.model.network;

import com.BluePrintHell.model.packets.Packet;
// import com.BluePrintHell.model.packets.ProtectedPacket; // این کلاس باید ایجاد شود
import com.BluePrintHell.model.packets.ProtectedPacket;
import javafx.geometry.Point2D;

public class VPNSystem extends NormalSystem { // می‌تواند از NormalSystem ارث‌بری کند

    public VPNSystem(String id, Point2D position) {
        super(id, position);
    }

    public VPNSystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        // اگر پکت از قبل محافظت‌شده نباشد، آن را تبدیل کن
        if (!(packet instanceof ProtectedPacket)) {
            ProtectedPacket protectedPacket = new ProtectedPacket(packet);
            super.receivePacket(protectedPacket);
        } else {
            super.receivePacket(packet); // اگر بود، بدون تغییر عبور بده
        }
    }
}