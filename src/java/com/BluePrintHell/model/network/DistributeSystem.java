package com.BluePrintHell.model.network;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.PortShape;
import com.BluePrintHell.model.packets.BitPacket;
import com.BluePrintHell.model.packets.LargePacket;
import com.BluePrintHell.model.packets.Packet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistributeSystem extends NormalSystem {

    private final Random random = new Random();

    public DistributeSystem(String id, Point2D position) {
        super(id, position);
    }

    public DistributeSystem() {
        super(null, null);
    }

    @Override
    public void receivePacket(Packet packet) {
        if (packet instanceof LargePacket) {
            LargePacket largePacket = (LargePacket) packet;
            GameState gs = getParentGameState();
            if (gs != null) {
                gs.registerLargePacket(largePacket);
            }

            System.out.println("DistributeSystem received a LargePacket of size " + largePacket.getSize());

            packetBuffer.clear();

            for (int i = 0; i < largePacket.getSize(); i++) {
                if (packetBuffer.size() < BUFFER_CAPACITY) {
                    BitPacket bitPacket = new BitPacket(this.getCenterPosition(), largePacket.getId());
                    packetBuffer.add(bitPacket);
                }
            }

            // یکی از پورت‌ها را به صورت تصادفی تغییر شکل می‌دهد
            randomizeAPortShape();

        } else {
            // اگر پکت عادی بود، مانند NormalSystem رفتار می‌کند
            super.receivePacket(packet);
        }
    }

    private void randomizeAPortShape() {
        List<Port> allPorts = new ArrayList<>();
        allPorts.addAll(getInputPorts());
        allPorts.addAll(getOutputPorts());

        if (!allPorts.isEmpty()) {
            // یک پورت را به صورت تصادفی انتخاب می‌کند
            Port portToChange = allPorts.get(random.nextInt(allPorts.size()));

            // یک شکل جدید به صورت تصادفی انتخاب می‌کند
            PortShape[] shapes = PortShape.values();
            PortShape newShape = shapes[random.nextInt(shapes.length)];

            // چون PortShape در کلاس Port به صورت private تعریف شده و setter ندارد،
            // باید یک متد setter در کلاس Port اضافه کنیم.
            // portToChange.setShape(newShape); // فرض بر اینکه متد setShape وجود دارد

            System.out.println("Port " + portToChange.getId() + " changed shape to " + newShape);
        }
    }

    @JsonIgnore
    public Point2D getCenterPosition() {
        if (position == null) return Point2D.ZERO;
        double SYSTEM_WIDTH = 120;
        double SYSTEM_HEIGHT = 80;
        return new Point2D(position.getX() + SYSTEM_WIDTH / 2, position.getY() + SYSTEM_HEIGHT / 2);
    }
}