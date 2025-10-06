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

public class SqlLiteDatabase implements  IDatabase {
    private final PluginLogger _logger = SkyBlockCore.Logger().withModule(MySqlDatabase.class);
    private SkyBlockConfig _config;
    private final Cache<@NotNull UUID, PlayerData> _playerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    //#region SQL Statements
    private String addPlayerDataSql;
    private String updatePlayerDataSql;
    private String removePlayerDataSql;
    private String getPlayerDataSql;
    //#endregion

    @Override
    public void load() {
        _config = SkyBlockCore.Config();
        update();
    }

    @Override
    public void update() {
        addPlayerDataSql = String.format("INSERT INTO %s_players (PlayerId, Experience, Level, Factories, " +
                        "CompletedFactories, MaxFactories, OnGoingFactories, FactoryResearch) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                _config.storageTablePrefix);

        updatePlayerDataSql = String.format("UPDATE %s_players SET Experience = ?, Level = ?, Factories = ?, " +
                "CompletedFactories = ?, MaxFactories = ?, OnGoingFactories = ?, FactoryResearch = ? " +
                "WHERE PlayerId = ?;", _config.storageTablePrefix);
        removePlayerDataSql = String.format("DELETE FROM %s_players WHERE PlayerId = ?;", _config.storageTablePrefix);
        getPlayerDataSql = String.format("SELECT * FROM %s_players WHERE PlayerId = ?;", _config.storageTablePrefix);
    }

    @Override
    public void unload() {/* ignored */}

    private Connection CreateConnection() {
        try {
            if (_config == null)
                _config = SkyBlockCore.Config();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/SkyBlockCore/%s.db", _config.storageFilename));
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void checkSchema() {
        try (Connection connection = CreateConnection()) {
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
                            "FactoryResearch INTEGER NOT NULL);",
                    _config.storageTablePrefix);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void addPlayerData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
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
                statement.executeUpdate();
            }

            PlayerData data = new PlayerData(playerId);
            _playerCache.put(playerId, data);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void updatePlayerData(PlayerData newData) {
        try (Connection connection = CreateConnection()) {
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
                statement.setString(8, newData.getUuid().toString());
                statement.executeUpdate();
            }

            _playerCache.put(newData.getUuid(), newData);
        } catch (Exception ex) {
            _logger.error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void removePlayerData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
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

    @Override
    public Optional<PlayerData> getPlayerData(UUID playerId) {
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }
        try (Connection connection = CreateConnection()) {
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
                                result.getInt("FactoryResearch")
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
}