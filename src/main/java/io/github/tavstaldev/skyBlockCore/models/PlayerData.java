package io.github.tavstaldev.skyBlockCore.models;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;

    private int experience;
    private int level;

    private int factories;
    private int completedFactories;
    private int maxFactories;
    private int ongoingFactories;
    private int factoryResearch;

    private boolean dailyRewardClaimed;
    private boolean weeklyRewardClaimed;
    private boolean hourlyRewardClaimed;

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

    public UUID getUuid() {
        return uuid;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFactories() {
        return factories;
    }

    public void setFactories(int factories) {
        this.factories = factories;
    }

    public int getCompletedFactories() {
        return completedFactories;
    }

    public void setCompletedFactories(int completedFactories) {
        this.completedFactories = completedFactories;
    }

    public int getMaxFactories() {
        return maxFactories;
    }

    public void setMaxFactories(int maxFactories) {
        this.maxFactories = maxFactories;
    }

    public int getOngoingFactories() {
        return ongoingFactories;
    }

    public void setOngoingFactories(int ongoingFactories) {
        this.ongoingFactories = ongoingFactories;
    }

    public int getFactoryResearch() {
        return factoryResearch;
    }

    public void setFactoryResearch(int factoryResearch) {
        this.factoryResearch = factoryResearch;
    }

    public boolean isDailyRewardClaimed() {
        return dailyRewardClaimed;
    }

    public void setDailyRewardClaimed(boolean dailyRewardClaimed) {
        this.dailyRewardClaimed = dailyRewardClaimed;
    }

    public boolean isWeeklyRewardClaimed() {
        return weeklyRewardClaimed;
    }

    public void setWeeklyRewardClaimed(boolean weeklyRewardClaimed) {
        this.weeklyRewardClaimed = weeklyRewardClaimed;
    }

    public boolean isHourlyRewardClaimed() {
        return hourlyRewardClaimed;
    }

    public void setHourlyRewardClaimed(boolean hourlyRewardClaimed) {
        this.hourlyRewardClaimed = hourlyRewardClaimed;
    }
}
