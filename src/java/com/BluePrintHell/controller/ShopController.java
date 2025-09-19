package com.BluePrintHell.controller;

import com.BluePrintHell.GameManager;
import com.BluePrintHell.model.ActivePowerUp;
import com.BluePrintHell.model.GameState;
import com.BluePrintHell.model.PowerUpType;
import com.BluePrintHell.model.packets.Packet;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ShopController {

    @FXML private Button buyOAtarButton;
    @FXML private Button buyOAiryamanButton;
    @FXML private Button buyOAnahitaButton;

    private GameState gameState;

    @FXML
    public void initialize() {
        this.gameState = GameManager.getInstance().getCurrentGameState();
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (gameState == null) return;
        int coins = gameState.getPlayerCoins();
        buyOAtarButton.setDisable(coins < 3);
        buyOAiryamanButton.setDisable(coins < 4);
        buyOAnahitaButton.setDisable(coins < 5);
    }

    @FXML
    private void onCloseClicked() {
        ScreenController.getInstance().activate(Screen.GAME);
    }

    @FXML
    private void onBuyOAtar() {
        if (gameState.getPlayerCoins() >= 3) {
            gameState.addCoins(-3);
            gameState.addPowerUp(new ActivePowerUp(PowerUpType.O_ATAR, 10)); // 10 ثانیه
            System.out.println("O' Atar activated!");
            updateButtonStates();
        }
    }

    @FXML
    private void onBuyOAiryaman() {
        if (gameState.getPlayerCoins() >= 4) {
            gameState.addCoins(-4);
            gameState.addPowerUp(new ActivePowerUp(PowerUpType.O_AIRYAMAN, 5)); // 5 ثانیه
            System.out.println("O' Airyaman activated!");
            updateButtonStates();
        }
    }

    @FXML
    private void onBuyOAnahita() {
        if (gameState.getPlayerCoins() >= 5) {
            gameState.addCoins(-5);
            for (Packet packet : gameState.getPackets()) {
                packet.addNoise(-packet.getNoise()); // نویز را صفر می‌کند
            }
            System.out.println("O' Anahita activated!");
            updateButtonStates();
        }
    }
}