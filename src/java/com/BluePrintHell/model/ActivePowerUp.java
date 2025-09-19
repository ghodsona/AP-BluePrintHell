package com.BluePrintHell.model;

public class ActivePowerUp {
    private PowerUpType type;
    private double remainingTime;

    public ActivePowerUp(PowerUpType type, double duration) {
        this.type = type;
        this.remainingTime = duration;
    }

    public void update(double deltaTime) {
        if (remainingTime > 0) {
            remainingTime -= deltaTime;
        }
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    public PowerUpType getType() {
        return type;
    }
}