package io.github.tavstaldev.skyBlockCore.database;

import io.github.tavstaldev.skyBlockCore.models.PlayerData;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents the interface for database operations in the SkyBlockCore plugin.
 * Provides methods for managing player data, rewards, and schema validation.
 */
public interface IDatabase {

    /**
     * Loads the database and initializes any required resources.
     */
    void load();

    /**
     * Updates the database with any pending changes.
     */
    void update();

    /**
     * Unloads the database and releases any allocated resources.
     */
    void unload();

    /**
     * Checks and ensures the database schema is up-to-date.
     */
    void checkSchema();

    /**
     * Adds a new player's data to the database.
     *
     * @param playerId The unique identifier of the player.
     */
    void addPlayerData(UUID playerId);

    /**
     * Updates an existing player's data in the database.
     *
     * @param newData The updated player data.
     */
    void updatePlayerData(PlayerData newData);

    /**
     * Removes a player's data from the database.
     *
     * @param playerId The unique identifier of the player.
     */
    void removePlayerData(UUID playerId);

    /**
     * Resets the daily rewards for all players in the database.
     */
    void resetDailyRewards();

    /**
     * Resets the weekly rewards for all players in the database.
     */
    void resetWeeklyRewards();

    /**
     * Resets the hourly rewards for all players in the database.
     */
    void resetHourlyRewards();

    /**
     * Retrieves a player's data from the database.
     *
     * @param playerId The unique identifier of the player.
     * @return An Optional containing the player's data, or empty if not found.
     */
    Optional<PlayerData> getPlayerData(UUID playerId);
}