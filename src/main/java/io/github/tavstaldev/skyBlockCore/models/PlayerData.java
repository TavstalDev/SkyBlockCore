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

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.experience = 0;
        this.level = 0;
        this.factories = 0;
        this.completedFactories = 0;
        this.maxFactories = 3;
        this.ongoingFactories = 0;
        this.factoryResearch = 0;
    }

    public PlayerData(UUID uuid, int experience, int level, int factories, int completedFactories, int maxFactories, int ongoingFactories, int factoryResearch) {
        this.uuid = uuid;
        this.experience = experience;
        this.level = level;
        this.factories = factories;
        this.completedFactories = completedFactories;
        this.maxFactories = maxFactories;
        this.ongoingFactories = ongoingFactories;
        this.factoryResearch = factoryResearch;
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
}
