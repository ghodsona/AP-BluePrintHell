package com.BluePrintHell.util;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.network.NetworkSystem;
import com.BluePrintHell.model.Port;
import com.BluePrintHell.model.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import java.io.File;
import java.io.IOException;
import javafx.geometry.Point2D;

public class SaveManager {

    private static final String AUTOSAVE_FILE = "autosave.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.addMixIn(Point2D.class, Point2DMixin.class);

        // ✅✅✅ تنظیمات کلیدی برای حل مشکل Reference ✅✅✅
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // ✅✅✅ اجبار Jackson برای استفاده از همه field ها ✅✅✅
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);

        // ✅✅✅ فعال کردن default constructor برای تمام کلاس‌ها ✅✅✅
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    public static void saveGame(GameState gameState) {
        try {
            // ✅✅✅ لاگ کردن اطلاعات قبل از Save ✅✅✅
            System.out.println("=== SAVING GAME ===");
            System.out.println("Systems: " + gameState.getSystems().size());
            System.out.println("Connections: " + gameState.getConnections().size());
            System.out.println("Packets: " + gameState.getPackets().size());

            for (NetworkSystem sys : gameState.getSystems()) {
                System.out.println("System: " + sys.getId() + " at " + sys.getPosition());
                System.out.println("  Input ports: " + sys.getInputPorts().size());
                System.out.println("  Output ports: " + sys.getOutputPorts().size());
            }

            for (Connection conn : gameState.getConnections()) {
                System.out.println("Connection: " + conn.getStartPort().getId() + " -> " + conn.getEndPort().getId());
            }

            objectMapper.writeValue(new File(AUTOSAVE_FILE), gameState);
            System.out.println("Game saved successfully!");
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static GameState loadGame() {
        File saveFile = new File(AUTOSAVE_FILE);
        if (saveFile.exists()) {
            try {
                System.out.println("=== LOADING GAME ===");
                GameState gameState = objectMapper.readValue(saveFile, GameState.class);

                // ✅✅✅ لاگ کردن اطلاعات بعد از Load اولیه ✅✅✅
                System.out.println("Initial load complete:");
                System.out.println("Systems loaded: " + (gameState.getSystems() != null ? gameState.getSystems().size() : "NULL"));
                System.out.println("Connections loaded: " + (gameState.getConnections() != null ? gameState.getConnections().size() : "NULL"));

                // ✅✅✅ بازسازی کامل روابط ✅✅✅
                reconstructAllRelationships(gameState);

                // ✅✅✅ لاگ نهایی ✅✅✅
                System.out.println("=== FINAL LOAD RESULT ===");
                System.out.println("Systems: " + gameState.getSystems().size());
                System.out.println("Connections: " + gameState.getConnections().size());

                for (NetworkSystem sys : gameState.getSystems()) {
                    System.out.println("Loaded System: " + sys.getId() + " at " + sys.getPosition());
                    System.out.println("  Input ports: " + sys.getInputPorts().size());
                    System.out.println("  Output ports: " + sys.getOutputPorts().size());
                }

                for (Connection conn : gameState.getConnections()) {
                    if (conn.getStartPort() != null && conn.getEndPort() != null) {
                        System.out.println("Loaded Connection: " + conn.getStartPort().getId() + " -> " + conn.getEndPort().getId());
                    } else {
                        System.out.println("WARNING: Connection with null ports detected!");
                    }
                }

                return gameState;
            } catch (IOException e) {
                System.err.println("Failed to load game: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * بازسازی کامل همه روابط بین object ها
     */
    private static void reconstructAllRelationships(GameState gameState) {
        if (gameState == null) return;

        System.out.println("Reconstructing relationships...");

        // ✅ مرحله ۱: بازسازی روابط پایه
        if (gameState.getSystems() != null) {
            for (NetworkSystem system : gameState.getSystems()) {
                if (system != null) {
                    system.setParentGameState(gameState);

                    // بازسازی روابط port ها
                    if (system.getInputPorts() != null) {
                        for (Port inputPort : system.getInputPorts()) {
                            if (inputPort != null) {
                                inputPort.setParentSystem(system);
                            }
                        }
                    }
                    if (system.getOutputPorts() != null) {
                        for (Port outputPort : system.getOutputPorts()) {
                            if (outputPort != null) {
                                outputPort.setParentSystem(system);
                            }
                        }
                    }
                }
            }
        }

        // ✅ مرحله ۲: پاک کردن Connection های معیوب
        if (gameState.getConnections() != null) {
            gameState.getConnections().removeIf(conn -> {
                if (conn == null) {
                    System.out.println("Removing null connection");
                    return true;
                }
                if (conn.getStartPort() == null) {
                    System.out.println("Removing connection with null start port");
                    return true;
                }
                if (conn.getEndPort() == null) {
                    System.out.println("Removing connection with null end port");
                    return true;
                }
                return false;
            });
        }

        // ✅ مرحله ۳: بازسازی Port connections
        if (gameState.getConnections() != null && gameState.getSystems() != null) {
            for (Connection conn : gameState.getConnections()) {
                if (conn.getStartPort() != null && conn.getEndPort() != null) {
                    // اتصال را دوباره به port ها متصل کن
                    conn.getStartPort().connect(conn);
                    conn.getEndPort().connect(conn);
                    System.out.println("Reconnected: " + conn.getStartPort().getId() + " -> " + conn.getEndPort().getId());
                }
            }
        }

        // ✅ مرحله ۴: بررسی نهایی و پاک کردن packet های معلق
        if (gameState.getPackets() != null) {
            gameState.getPackets().clear(); // همه packet های قدیمی را پاک کن
        }

        // ✅ مرحله ۵: Reset کردن زمان بازی تا از شروع شبیه‌سازی جلوگیری کنیم
        gameState.resetGameTime();

        System.out.println("Relationship reconstruction complete!");
    }

    public static boolean hasAutoSave() {
        File file = new File(AUTOSAVE_FILE);
        boolean exists = file.exists() && file.length() > 0;
        System.out.println("AutoSave check: " + exists + " (file size: " + (file.exists() ? file.length() : 0) + " bytes)");
        return exists;
    }

    public static void deleteAutoSave() {
        File saveFile = new File(AUTOSAVE_FILE);
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("Autosave file deleted successfully.");
            } else {
                System.err.println("Failed to delete autosave file.");
            }
        }
    }
}