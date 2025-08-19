package com.BluePrintHell.util;

import com.BluePrintHell.model.leveldata.LevelData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class LevelLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static LevelData loadLevel(int levelNumber) {
        String levelFileName = "/com/BluePrintHell/data/levels/level-" + levelNumber + ".json";
        try (InputStream inputStream = LevelLoader.class.getResourceAsStream(levelFileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot find level file: " + levelFileName);
            }
            // فایل JSON را بخوان و آن را به یک آبجکت LevelData تبدیل کن
            return objectMapper.readValue(inputStream, LevelData.class);
        } catch (Exception e) {
            e.printStackTrace();
            // در یک برنامه واقعی، باید خطا را بهتر مدیریت کرد
            return null;
        }
    }
}
