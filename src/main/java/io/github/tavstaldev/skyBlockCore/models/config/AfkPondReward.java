package io.github.tavstaldev.skyBlockCore.models.config;

public class AfkPondReward {
    public long interval;
    public String command;

    public AfkPondReward(long interval, String command) {
        this.interval = interval;
        this.command = command;
    }
}
