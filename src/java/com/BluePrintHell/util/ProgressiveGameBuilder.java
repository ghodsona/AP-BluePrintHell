package com.BluePrintHell.util;

import com.BluePrintHell.model.*;
import com.BluePrintHell.model.leveldata.LevelData;
import com.BluePrintHell.model.leveldata.PortData;
import com.BluePrintHell.model.leveldata.SystemData;
import com.BluePrintHell.model.network.*;
import javafx.geometry.Point2D;

import java.util.ArrayList;

public class ProgressiveGameBuilder {

    public static GameState buildProgressiveLevel(LevelData newLevelData, GameState previousGameState) {
        GameState gameState;

        if (previousGameState == null) {
            gameState = new GameState();
            gameState.setLevelNumber(newLevelData.getLevelNumber());
            gameState.setPlayerCoins(newLevelData.getInitialCoins());
            gameState.setPlayerWireLength(newLevelData.getInitialWireLength());
        } else {
            gameState = previousGameState;
            gameState.setLevelNumber(newLevelData.getLevelNumber());

            for (Connection conn : gameState.getConnections()) {
                conn.getBendPoints().clear();
            }

            gameState.getPackets().clear();
            gameState.resetGameTime();
        }

        for (SystemData sysData : newLevelData.getSystems()) {
            boolean systemExists = gameState.getSystems().stream()
                    .anyMatch(existingSystem -> existingSystem.getId().equals(sysData.getId()));

            if (!systemExists) {
                Point2D position = new Point2D(sysData.getX(), sysData.getY());
                NetworkSystem newSystem = createSystemFromData(sysData.getId(), sysData.getType(), position);
                newSystem.setParentGameState(gameState);
                createPortsForSystem(newSystem, sysData);
                gameState.addSystem(newSystem);
            }
        }

        if (newLevelData.getSpawnEvents() != null) {
            gameState.setSpawnEvents(new ArrayList<>(newLevelData.getSpawnEvents()));
        }

        return gameState;
    }

    private static NetworkSystem createSystemFromData(String id, String type, Point2D position) {
        switch (type.toUpperCase()) {
            case "REFERENCE":
                return new ReferenceSystem(id, position);
            case "NORMAL":
                return new NormalSystem(id, position);
            case "DISTRIBUTESYSTEM":
                return new DistributeSystem(id, position);
            case "MERGESYSTEM":
                return new MergeSystem(id, position);
            case "SABOTEURSYSTEM":
                return new SaboteurSystem(id, position);
            case "VPNSYSTEM":
                return new VPNSystem(id, position);
            case "SPYSYSTEM":
                return new SpySystem(id, position);
            case "ANTITROJANSYSTEM":
                return new AntiTrojanSystem(id, position);
            default:
                throw new IllegalArgumentException("Unknown system type: " + type);
        }
    }

    private static void createPortsForSystem(NetworkSystem system, SystemData sysData) {
        for (PortData portData : sysData.getInputPorts()) {
            PortShape shape = PortShape.valueOf(portData.getShape().toUpperCase());
            Port newPort = new Port(portData.getId(), system, PortType.INPUT, shape);
            system.getInputPorts().add(newPort);
        }

        for (PortData portData : sysData.getOutputPorts()) {
            PortShape shape = PortShape.valueOf(portData.getShape().toUpperCase());
            Port newPort = new Port(portData.getId(), system, PortType.OUTPUT, shape);
            system.getOutputPorts().add(newPort);
        }
    }
}