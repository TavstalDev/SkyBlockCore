package io.github.tavstaldev.skyBlockCore.models.config;

/**
 * Represents a reward configuration for the AFK pond.
 * Each reward is defined by an interval and a command to execute.
 */
public class AfkPondReward {
    public long interval; // The interval in minutes at which the reward is given.
    public String command; // The command to execute as the reward.

    /**
     * Constructs a new AfkPondReward instance.
     *
     * @param interval The interval in minutes at which the reward is given.
     * @param command  The command to execute as the reward.
     */
    public AfkPondReward(long interval, String command) {
        this.interval = interval;
        this.command = command;
    }
}