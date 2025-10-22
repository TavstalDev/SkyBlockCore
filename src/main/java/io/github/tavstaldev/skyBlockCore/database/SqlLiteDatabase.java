package io.github.tavstaldev.skyBlockCore.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.skyBlockCore.SkyBlockConfig;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.models.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implements the IDatabase interface using an SQLite database.
 * Provides methods for managing player data, rewards, and schema validation.
 * Utilizes Caffeine for caching player data.
 */
public class SqlLiteDatabase implements IDatabase {
    private final PluginLogger _logger = SkyBlockCore.logger().withModule(SqlLiteDatabase.class); // Logger for database operations.
    private SkyBlockConfig _config; // Configuration for database settings.
    private final Cache<@NotNull UUID, PlayerData> _playerCache = Caffeine.newBuilder()
            .maximumSize(1000) // Maximum cache size.
            .expireAfterWrite(5, TimeUnit.MINUTES) // Cache expiration time.
            .build();

    //#region SQL Statements
    private String addPlayerDataSql; // SQL statement for adding player data.
    private String updatePlayerDataSql; // SQL statement for updating player data.
    private String removePlayerDataSql; // SQL statement for removing player data.
    private String getPlayerDataSql; // SQL statement for retrieving player data.
    private String resetDailyRewardsSql; // SQL statement for resetting daily rewards.
    private String resetWeeklyRewardsSql; // SQL statement for resetting weekly rewards.
    private String resetHourlyRewardsSql; // SQL statement for resetting hourly rewards.
    //#endregion

    /**
     * Loads the database by initializing the configuration and updating SQL statements.
     */
    @Override
    public void load() {
        _config = SkyBlockCore.config();
        update();
    }

    /**
     * Updates the SQL statements based on the current configuration.
     */
    @Override
    public void update() {
        addPlayerDataSql = String.format("INSERT INTO %s_players (PlayerId, Experience, Level, Factories, " +
                        "CompletedFactories, MaxFactories, OnGoingFactories, FactoryResearch, DailyRewardClaimed, WeeklyRewardClaimed, HourlyRewardClaimed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                _config.storageTablePrefix);

        updatePlayerDataSql = String.format("UPDATE %s_players SET Experience = ?, Level = ?, Factories = ?, " +
                "CompletedFactories = ?, MaxFactories = ?, OnGoingFactories = ?, FactoryResearch = ?, " +
                "DailyRewardClaimed = ?, WeeklyRewardClaimed = ?, HourlyRewardClaimed = ? " +
                "WHERE PlayerId = ?;", _config.storageTablePrefix);
        removePlayerDataSql = String.format("DELETE FROM %s_players WHERE PlayerId = ?;", _config.storageTablePrefix);
        getPlayerDataSql = String.format("SELECT * FROM %s_players WHERE PlayerId = ?;", _config.storageTablePrefix);

        resetDailyRewardsSql = String.format("UPDATE %s_players SET DailyRewardClaimed = 0;", _config.storageTablePrefix);
        resetWeeklyRewardsSql = String.format("UPDATE %s_players SET WeeklyRewardClaimed = 0;", _config.storageTablePrefix);
        resetHourlyRewardsSql = String.format("UPDATE %s_players SET HourlyRewardClaimed = 0;", _config.storageTablePrefix);
    }

    /**
     * Unloads the database. Currently, this method does nothing.
     */
    @Override
    public void unload() {/* ignored */}

    /**
     * Creates a connection to the SQLite database.
     *
     * @return A Connection object to the SQLite database, or null if an error occurs.
     */
    private Connection createConnection() {
        try {
            if (_config == null)
                _config = SkyBlockCore.config();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/SkyBlockCore/%s.db", _config.storageFilename));
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the database schema is up-to-date by creating necessary tables if they do not exist.
     */
    @Override
    public void checkSchema() {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot check schema.");
                return;
            }

            // Players table
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_players (" +
                            "PlayerId VARCHAR(36) PRIMARY KEY, " +
                            "Experience INTEGER NOT NULL, " +
                            "Level INTEGER NOT NULL, " +
                            "Factories INTEGER NOT NULL, " +
                            "CompletedFactories INTEGER NOT NULL, " +
                            "MaxFactories INTEGER NOT NULL, " +
                            "OnGoingFactories INTEGER NOT NULL, " +
                            "FactoryResearch INTEGER NOT NULL, " +
                            "DailyRewardClaimed INTEGER NOT NULL, " +
                            "WeeklyRewardClaimed INTEGER NOT NULL, " +
                            "HourlyRewardClaimed INTEGER NOT NULL);",
                    _config.storageTablePrefix);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Adds a new player's data to the database and caches it.
     *
     * @param playerId The unique identifier of the player.
     */
    @Override
    public void addPlayerData(UUID playerId) {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot add player data.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(addPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                statement.setInt(2, 0); // Experience
                statement.setInt(3, 0); // Level
                statement.setInt(4, 0); // Factories
                statement.setInt(5, 0); // CompletedFactories
                statement.setInt(6, 3); // MaxFactories
                statement.setInt(7, 0); // OnGoingFactories
                statement.setInt(8, 0); // FactoryResearch
                statement.setInt(9, 0); // DailyRewardClaimed
                statement.setInt(10, 0); // WeeklyRewardClaimed
                statement.setInt(11, 0); // HourlyRewardClaimed
                statement.executeUpdate();
            }

            PlayerData data = new PlayerData(playerId);
            _playerCache.put(playerId, data);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates an existing player's data in the database and cache.
     *
     * @param newData The updated player data.
     */
    @Override
    public void updatePlayerData(PlayerData newData) {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot update player data.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(updatePlayerDataSql)) {
                statement.setInt(1, newData.getExperience()); // Experience
                statement.setInt(2, newData.getLevel()); // Level
                statement.setInt(3, newData.getFactories()); // Factories
                statement.setInt(4, newData.getCompletedFactories()); // CompletedFactories
                statement.setInt(5, newData.getMaxFactories()); // MaxFactories
                statement.setInt(6, newData.getOngoingFactories()); // OnGoingFactories
                statement.setInt(7, newData.getFactoryResearch()); // FactoryResearch
                statement.setInt(8, newData.isDailyRewardClaimed() ? 1 : 0); // DailyRewardClaimed
                statement.setInt(9, newData.isWeeklyRewardClaimed() ? 1 : 0); // WeeklyRewardClaimed
                statement.setInt(10, newData.isHourlyRewardClaimed() ? 1 : 0); // HourlyRewardClaimed
                statement.setString(11, newData.getUuid().toString());
                statement.executeUpdate();
            }

            _playerCache.put(newData.getUuid(), newData);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a player's data from the database and cache.
     *
     * @param playerId The unique identifier of the player.
     */
    @Override
    public void removePlayerData(UUID playerId) {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot remove player data.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(removePlayerDataSql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                _playerCache.invalidate(playerId);
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while removing player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves a player's data from the database or cache.
     *
     * @param playerId The unique identifier of the player.
     * @return An Optional containing the player's data, or empty if not found.
     */
    @Override
    public Optional<PlayerData> getPlayerData(UUID playerId) {
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot get player data.");
                return Optional.empty();
            }

            try (PreparedStatement statement = connection.prepareStatement(getPlayerDataSql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new PlayerData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getInt("Experience"),
                                result.getInt("Level"),
                                result.getInt("Factories"),
                                result.getInt("CompletedFactories"),
                                result.getInt("MaxFactories"),
                                result.getInt("OnGoingFactories"),
                                result.getInt("FactoryResearch"),
                                result.getInt("DailyRewardClaimed") == 1,
                                result.getInt("WeeklyRewardClaimed") == 1,
                                result.getInt("HourlyRewardClaimed") == 1
                        );
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while finding player data...\n%s", ex.getMessage()));
            return Optional.empty();
        }

        if (data != null) {
            _playerCache.put(playerId, data);
        }
        return Optional.ofNullable(data);
    }

    /**
     * Resets the daily rewards for all players in the database and invalidates the cache.
     */
    @Override
    public void resetDailyRewards() {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot reset daily rewards.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(resetDailyRewardsSql)) {
                statement.executeUpdate();
            }

            _playerCache.invalidateAll();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while resetting daily rewards...\n%s", ex.getMessage()));
        }
    }

    /**
     * Resets the weekly rewards for all players in the database and invalidates the cache.
     */
    @Override
    public void resetWeeklyRewards() {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot reset weekly rewards.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(resetWeeklyRewardsSql)) {
                statement.executeUpdate();
            }

            _playerCache.invalidateAll();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while resetting weekly rewards...\n%s", ex.getMessage()));
        }
    }

    /**
     * Resets the hourly rewards for all players in the database and invalidates the cache.
     */
    @Override
    public void resetHourlyRewards() {
        try (Connection connection = createConnection()) {
            if (connection == null) {
                _logger.error("Connection is null, cannot reset hourly rewards.");
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(resetHourlyRewardsSql)) {
                statement.executeUpdate();
            }

            _playerCache.invalidateAll();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while resetting hourly rewards...\n%s", ex.getMessage()));
        }
    }
}