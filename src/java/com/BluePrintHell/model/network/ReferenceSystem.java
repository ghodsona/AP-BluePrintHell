package com.BluePrintHell.model.network;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.packets.Packet;
import javafx.geometry.Point2D;
public class ReferenceSystem extends NetworkSystem {
    public ReferenceSystem(String id, Point2D position) { super(id, position); }
    @Override
    public void update(double deltaTime) {
        // اگر پکتی در بافر منتظر است تا ارسال شود
        if (!packetBuffer.isEmpty()) {
            // یک پورت خروجی پیدا کن که به یک سیم متصل باشد
            for (Port outputPort : outputPorts) {
                if (outputPort.isConnected()) {
                    // پکت را از صف بافر خارج کن
                    Packet packetToLaunch = packetBuffer.poll();

                    if (packetToLaunch != null) {
                        // ۱. موقعیت اولیه پکت را به مرکز پورت خروجی منتقل کن
                        packetToLaunch.setPosition(new Point2D(
                                outputPort.getPosition().getX() + 6, // PORT_SIZE / 2
                                outputPort.getPosition().getY() + 6  // PORT_SIZE / 2
                        ));

                        // ۲. به پکت بگو که روی کدام سیم و به سمت کدام مقصد حرکت کند
                        packetToLaunch.launch(outputPort.getAttachedConnection());

                        // ۳. پکت را به لیست اصلی پکت‌های "در حال حرکت" در GameState اضافه کن
                        this.getParentGameState().addPacket(packetToLaunch);

                        System.out.println(">>> System " + this.id + " launched a packet!");

                        // بعد از ارسال یک پکت، از حلقه خارج شو تا در هر فریم فقط یک پکت ارسال شود
                        break;
                    }
                }
            }
        }
    }
}