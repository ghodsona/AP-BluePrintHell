package com.BluePrintHell.util;

import com.BluePrintHell.model.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

public class SaveManager {

    private static final String AUTOSAVE_FILE = "autosave.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void saveGame(GameState gameState) {
        try {
            objectMapper.writeValue(new File(AUTOSAVE_FILE), gameState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameState loadGame() {
        File saveFile = new File(AUTOSAVE_FILE);
        if (saveFile.exists()) {
            try {
                return objectMapper.readValue(saveFile, GameState.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static boolean hasAutoSave() {
        return new File(AUTOSAVE_FILE).exists();
    }

    public static void deleteAutoSave() {
        File saveFile = new File(AUTOSAVE_FILE);
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }
}