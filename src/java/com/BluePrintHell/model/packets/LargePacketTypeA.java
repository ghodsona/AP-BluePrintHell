package com.BluePrintHell.model.packets;

import com.BluePrintHell.model.Connection;
import com.BluePrintHell.model.PortShape;
import javafx.geometry.Point2D;

public class LargePacketTypeA extends LargePacket {
    public LargePacketTypeA(Point2D startPosition) {
        super(startPosition, 8);
        // حرکت با سرعت ثابت روی خطوط صاف
        this.currentSpeed = 80;
    }

    public LargePacketTypeA() { super(null, 8); }

    @Override
    public void update(double deltaTime) {
        // TODO: منطق حرکت شتاب‌دار روی انحناها باید اینجا پیاده‌سازی شود
        super.update(deltaTime);
    }

    @Override
    public int getCoinValue() { return 8; }

    @Override
    public boolean isCompatibleWith(PortShape shape) { return false; }
}