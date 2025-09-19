package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class ProtectedPacket extends Packet {
    private Packet originalPacket; // پکت اصلی که پنهان شده است
    private static final double BASE_SPEED = 110; // سرعت حرکت یکنواخت

    public ProtectedPacket(Packet originalPacket) {
        // موقعیت اولیه را از پکت اصلی می‌گیرد
        super(originalPacket.getPosition(), originalPacket.getSize());
        this.originalPacket = originalPacket;
        this.currentSpeed = BASE_SPEED;
    }

    public ProtectedPacket() { // برای سریالایز کردن نیاز است
        super(null, 0);
    }

    public Packet getOriginalPacket() {
        return originalPacket;
    }

    @Override
    public int getCoinValue() {
        // مقدار سکه پکت اصلی را برمی‌گرداند
        return originalPacket.getCoinValue();
    }

    @Override
    public boolean isCompatibleWith(PortShape shape) {
        // پکت محافظت‌شده با همه پورت‌ها رفتار یکسانی دارد و سازگاری برای آن بی‌معناست
        return true;
    }
}