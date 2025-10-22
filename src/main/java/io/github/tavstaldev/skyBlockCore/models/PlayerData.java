package io.github.tavstaldev.skyBlockCore.models;

import java.util.UUID;

/**
 * Represents the data associated with a player in the SkyBlockCore plugin.
 * This includes experience, level, factory-related statistics, and reward statuses.
 */
public class PlayerData {
    private final UUID uuid; // The unique identifier of the player.

    private int experience; // The player's current experience points.
    private int level; // The player's current level.

    private int factories; // The total number of factories owned by the player.
    private int completedFactories; // The number of factories completed by the player.
    private int maxFactories; // The maximum number of factories the player can own.
    private int ongoingFactories; // The number of factories currently in progress.
    private int factoryResearch; // The player's factory research progress.

    private boolean dailyRewardClaimed; // Whether the player has claimed the daily reward.
    private boolean weeklyRewardClaimed; // Whether the player has claimed the weekly reward.
    private boolean hourlyRewardClaimed; // Whether the player has claimed the hourly reward.

    /**
     * Constructs a new PlayerData instance with default values.
     *
     * @param uuid The unique identifier of the player.
     */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.experience = 0;
        this.level = 0;
        this.factories = 0;
        this.completedFactories = 0;
        this.maxFactories = 3;
        this.ongoingFactories = 0;
        this.factoryResearch = 0;
        this.dailyRewardClaimed = false;
        this.weeklyRewardClaimed = false;
        this.hourlyRewardClaimed = false;
    }

    /**
     * Constructs a new PlayerData instance with specified values.
     *
     * @param uuid                The unique identifier of the player.
     * @param experience          The player's experience points.
     * @param level               The player's level.
     * @param factories           The total number of factories owned by the player.
     * @param completedFactories  The number of factories completed by the player.
     * @param maxFactories        The maximum number of factories the player can own.
     * @param ongoingFactories    The number of factories currently in progress.
     * @param factoryResearch     The player's factory research progress.
     * @param dailyRewardClaimed  Whether the player has claimed the daily reward.
     * @param weeklyRewardClaimed Whether the player has claimed the weekly reward.
     * @param hourlyRewardClaimed Whether the player has claimed the hourly reward.
     */
    public PlayerData(UUID uuid, int experience, int level, int factories, int completedFactories, int maxFactories, int ongoingFactories, int factoryResearch, boolean dailyRewardClaimed, boolean weeklyRewardClaimed, boolean hourlyRewardClaimed) {
        this.uuid = uuid;
        this.experience = experience;
        this.level = level;
        this.factories = factories;
        this.completedFactories = completedFactories;
        this.maxFactories = maxFactories;
        this.ongoingFactories = ongoingFactories;
        this.factoryResearch = factoryResearch;
        this.dailyRewardClaimed = dailyRewardClaimed;
        this.weeklyRewardClaimed = weeklyRewardClaimed;
        this.hourlyRewardClaimed = hourlyRewardClaimed;
    }

    /**
     * Gets the unique identifier of the player.
     *
     * @return The player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the player's experience points.
     *
     * @return The player's experience.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the player's experience points.
     *
     * @param experience The new experience value.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Gets the player's level.
     *
     * @return The player's level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the player's level.
     *
     * @param level The new level value.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Gets the total number of factories owned by the player.
     *
     * @return The number of factories.
     */
    public int getFactories() {
        return factories;
    }

    /**
     * Sets the total number of factories owned by the player.
     *
     * @param factories The new number of factories.
     */
    public void setFactories(int factories) {
        this.factories = factories;
    }

    /**
     * Gets the number of factories completed by the player.
     *
     * @return The number of completed factories.
     */
    public int getCompletedFactories() {
        return completedFactories;
    }

    /**
     * Sets the number of factories completed by the player.
     *
     * @param completedFactories The new number of completed factories.
     */
    public void setCompletedFactories(int completedFactories) {
        this.completedFactories = completedFactories;
    }

    /**
     * Gets the maximum number of factories the player can own.
     *
     * @return The maximum number of factories.
     */
    public int getMaxFactories() {
        return maxFactories;
    }

    /**
     * Sets the maximum number of factories the player can own.
     *
     * @param maxFactories The new maximum number of factories.
     */
    public void setMaxFactories(int maxFactories) {
        this.maxFactories = maxFactories;
    }

    /**
     * Gets the number of factories currently in progress.
     *
     * @return The number of ongoing factories.
     */
    public int getOngoingFactories() {
        return ongoingFactories;
    }

    /**
     * Sets the number of factories currently in progress.
     *
     * @param ongoingFactories The new number of ongoing factories.
     */
    public void setOngoingFactories(int ongoingFactories) {
        this.ongoingFactories = ongoingFactories;
    }

    /**
     * Gets the player's factory research progress.
     *
     * @return The factory research progress.
     */
    public int getFactoryResearch() {
        return factoryResearch;
    }

    /**
     * Sets the player's factory research progress.
     *
     * @param factoryResearch The new factory research progress.
     */
    public void setFactoryResearch(int factoryResearch) {
        this.factoryResearch = factoryResearch;
    }

    /**
     * Checks if the player has claimed the daily reward.
     *
     * @return True if the daily reward is claimed, false otherwise.
     */
    public boolean isDailyRewardClaimed() {
        return dailyRewardClaimed;
    }

    /**
     * Sets the daily reward claim status.
     *
     * @param dailyRewardClaimed The new daily reward claim status.
     */
    public void setDailyRewardClaimed(boolean dailyRewardClaimed) {
        this.dailyRewardClaimed = dailyRewardClaimed;
    }

    /**
     * Checks if the player has claimed the weekly reward.
     *
     * @return True if the weekly reward is claimed, false otherwise.
     */
    public boolean isWeeklyRewardClaimed() {
        return weeklyRewardClaimed;
    }

    /**
     * Sets the weekly reward claim status.
     *
     * @param weeklyRewardClaimed The new weekly reward claim status.
     */
    public void setWeeklyRewardClaimed(boolean weeklyRewardClaimed) {
        this.weeklyRewardClaimed = weeklyRewardClaimed;
    }

    /**
     * Checks if the player has claimed the hourly reward.
     *
     * @return True if the hourly reward is claimed, false otherwise.
     */
    public boolean isHourlyRewardClaimed() {
        return hourlyRewardClaimed;
    }

    /**
     * Sets the hourly reward claim status.
     *
     * @param hourlyRewardClaimed The new hourly reward claim status.
     */
    public void setHourlyRewardClaimed(boolean hourlyRewardClaimed) {
        this.hourlyRewardClaimed = hourlyRewardClaimed;
    }
}