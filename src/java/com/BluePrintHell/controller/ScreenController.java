package com.BluePrintHell.controller;

import com.BluePrintHell.model.GameState;
import com.BluePrintHell.view.GameScreenView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ScreenController {
    private static ScreenController instance;
    private final Stage stage;
    private final StackPane root = new StackPane();

    // یک کلاس داخلی برای نگهداری زوج "صفحه" و "کنترلر" آن
    private static class ScreenData {
        Parent node;
        Object controller;

        ScreenData(Parent node, Object controller) {
            this.node = node;
            this.controller = controller;
        }
    }

    private final Map<Screen, ScreenData> screenCache = new HashMap<>();

    private ScreenController(Stage stage) {
        this.stage = stage;
        Scene scene = new Scene(root);
        String cssPath = "/com/BluePrintHell/view/styles/app.css";
        URL cssUrl = getClass().getResource(cssPath);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }

    public static void initialize(Stage stage) {
        if (instance == null) {
            instance = new ScreenController(stage);
        }
    }

    public static ScreenController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ScreenController has not been initialized.");
        }
        return instance;
    }

    /**
     * یک صفحه را بر اساس enum آن بارگذاری کرده و هم خود صفحه و هم کنترلرش را در کش ذخیره می‌کند.
     * @param screen صفحه‌ای که باید بارگذاری شود
     * @throws IOException اگر فایل FXML پیدا نشود
     */
    public void loadScreen(Screen screen) throws IOException {
        if (screen == null || screenCache.containsKey(screen)) return;

        URL fxmlUrl = getClass().getResource(screen.getFxmlPath());
        if (fxmlUrl == null) {
            throw new IOException("Cannot find FXML file: " + screen.getFxmlPath());
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent screenNode = loader.load();
        Object controller = loader.getController(); // << کنترلر را از لودر می‌گیریم

        screenCache.put(screen, new ScreenData(screenNode, controller)); // << هر دو را ذخیره می‌کنیم
    }

    /**
     * یک صفحه را فعال می‌کند و کنترلر آن را برمی‌گرداند تا بتوان قبل از نمایش، آن را تنظیم کرد.
     * @param screen صفحه‌ای که باید فعال شود
     * @param initializer یک تابع که کنترلر را به عنوان ورودی می‌گیرد (برای پاس دادن دیتا)
     * @param <T> نوع کنترلر
     */
    public <T> void activate(Screen screen, Consumer<T> initializer) {
        // صفحه بازی یک استثنا است چون به صورت دستی ساخته می‌شود
        if (screen == Screen.GAME) {
            activateGameScreen();
            return;
        }

        try {
            // اگر صفحه قبلا بارگذاری نشده، آن را بارگذاری کن
            if (!screenCache.containsKey(screen)) {
                loadScreen(screen);
            }

            ScreenData data = screenCache.get(screen);
            if (data != null) {
                // قبل از نمایش صفحه، تابع initializer را روی کنترلر آن اجرا کن
                if (initializer != null && data.controller != null) {
                    initializer.accept((T) data.controller);
                }
                root.getChildren().setAll(data.node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * نسخه ساده‌تر activate برای صفحاتی که نیاز به پاس دادن دیتا ندارند.
     * @param screen صفحه‌ای که باید فعال شود
     */
    public void activate(Screen screen) {
        activate(screen, null); // initializer را null پاس می‌دهیم
    }

    // متد ساخت صفحه بازی همچنان جدا باقی می‌ماند چون از FXML ساخته نمی‌شود
    private void activateGameScreen() {
        GameController gameController = new GameController();
        GameScreenView gameView = new GameScreenView(gameController);
        gameController.initializeGame(gameView);
        root.getChildren().setAll(gameView);
    }
}