package com.BluePrintHell.controller;

import javafx.fxml.FXML;

public class ShopController {

    @FXML
    private void onCloseClicked() {
        System.out.println("Closing shop and returning to game...");
        // کاربر را به صفحه بازی برمی‌گردانیم
        ScreenController.getInstance().activate(Screen.GAME);
    }

    // TODO: برای هر دکمه "Buy" یک متد جداگانه بنویسید
    @FXML
    private void onBuyItem1() {
        System.out.println("Item 1 purchased!");
        // منطق خرید آیتم
    }
}