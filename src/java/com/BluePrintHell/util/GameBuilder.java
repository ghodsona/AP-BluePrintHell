package com.BluePrintHell.util;

import com.BluePrintHell.model.*;
import com.BluePrintHell.model.leveldata.LevelData;
import com.BluePrintHell.model.leveldata.PortData;
import com.BluePrintHell.model.leveldata.SystemData;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.network.NormalSystem;
import com.BluePrintHell.model.network.ReferenceSystem;
import javafx.geometry.Point2D;

import java.util.ArrayList;

public class GameBuilder {

    public static GameState buildFrom(LevelData data) {
        GameState gameState = new GameState();

        // ۱. تنظیم اطلاعات اولیه مرحله
        gameState.setLevelNumber(data.getLevelNumber());
        gameState.setPlayerCoins(data.getInitialCoins());
        gameState.setPlayerWireLength(data.getInitialWireLength());

        // ۲. ساختن سیستم‌های شبکه از روی دیتا
        for (SystemData sysData : data.getSystems()) {
            Point2D position = new Point2D(sysData.getX(), sysData.getY());
            NetworkSystem newSystem = createSystemFromData(sysData.getId(), sysData.getType(), position);
            newSystem.setParentGameState(gameState);
            createPortsForSystem(newSystem, sysData);
            gameState.addSystem(newSystem);
        }

        // ==========================================================
        // === این خط اصلاح شده و بسیار مهم است ===
        // ==========================================================
        // ۳. اضافه کردن رویدادهای تولید پکت به GameState
        if (data.getSpawnEvents() != null) {
            gameState.setSpawnEvents(new ArrayList<>(data.getSpawnEvents()));
        }
        // ==========================================================

        return gameState;
    }

    private static NetworkSystem createSystemFromData(String id, String type, Point2D position) {
        // با استفاده از نوع سیستم، کلاس مربوطه را new می‌کنیم
        switch (type.toUpperCase()) {
            case "REFERENCE":
                return new ReferenceSystem(id, position);
            case "NORMAL":
                return new NormalSystem(id, position);
            // TODO: انواع دیگر سیستم‌ها (VPN, SABOTEUR, ...) اینجا اضافه می‌شوند
            default:
                throw new IllegalArgumentException("Unknown system type: " + type);
        }
    }

    private static void createPortsForSystem(NetworkSystem system, SystemData sysData) {
        // پورت‌های ورودی
        for (PortData portData : sysData.getInputPorts()) {
            PortShape shape = PortShape.valueOf(portData.getShape().toUpperCase());
            Port newPort = new Port(portData.getId(), system, PortType.INPUT, shape);
            system.getInputPorts().add(newPort);
            System.out.println("DEBUG: Created INPUT port '" + newPort.getId() + "' for system '" + system.getId() + "'");
        }

        // پورت‌های خروجی
        for (PortData portData : sysData.getOutputPorts()) {
            PortShape shape = PortShape.valueOf(portData.getShape().toUpperCase());
            Port newPort = new Port(portData.getId(), system, PortType.OUTPUT, shape);
            system.getOutputPorts().add(newPort);
            System.out.println("DEBUG: Created OUTPUT port '" + newPort.getId() + "' for system '" + system.getId() + "'");
        }
    }
}