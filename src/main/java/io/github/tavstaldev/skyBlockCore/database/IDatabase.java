package io.github.tavstaldev.skyBlockCore.database;

import io.github.tavstaldev.skyBlockCore.models.PlayerData;

import java.util.Optional;
import java.util.UUID;

public interface IDatabase {

    void load();

    void update();

    void unload();

    void checkSchema();

    void addPlayerData(UUID playerId);

    void updatePlayerData(PlayerData newData);

    void removePlayerData(UUID playerId);

    void resetDailyRewards();
    void resetWeeklyRewards();
    void resetHourlyRewards();

    Optional<PlayerData> getPlayerData(UUID playerId);
}
