package io.github.tavstaldev.skyBlockCore.managers;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages player-related data such as AFK pond status and join times.
 * Provides methods to add, remove, and retrieve player data.
 */
public class PlayerCacheManager {
    // Stores the players currently in the AFK pond and their respective times.
    private static final Map<UUID, LocalDateTime> _playersInAfkPond = new HashMap<>();
    private static final Map<UUID, Map<String, LocalDateTime>> _playersAfkRewards = new HashMap<>();
    // Stores the join times of players.
    private static final Map<UUID, LocalDateTime> _playerNextGameRewardTime = new HashMap<>();

    //#region Afk Pond

    /**
     * Adds a player to the AFK pond with the specified time.
     *
     * @param playerId The unique identifier of the player.
     * @param time     The time the player entered the AFK pond.
     */
    public static void addToAfkPond(UUID playerId, LocalDateTime time) {

        _playersInAfkPond.put(playerId, time);
        var config = SkyBlockCore.config();
        for (var reward : config.afkPondRewards) {
            addAfkRewardTime(playerId, reward.command, time.plusMinutes(reward.interval));
        }
    }

    /**
     * Removes a player from the AFK pond.
     *
     * @param playerId The unique identifier of the player.
     */
    public static void removeFromAfkPond(UUID playerId) {
        _playersInAfkPond.remove(playerId);
        clearAfkRewardTimes(playerId);
    }

    /**
     * Retrieves the time a player entered the AFK pond.
     *
     * @param playerId The unique identifier of the player.
     * @return The time the player entered the AFK pond, or null if the player is not in the pond.
     */
    public static @Nullable LocalDateTime getAfkTime(UUID playerId) {
        return _playersInAfkPond.get(playerId);
    }

    /**
     * Retrieves an unmodifiable map of players currently in the AFK pond and their respective times.
     *
     * @return An unmodifiable map of players in the AFK pond.
     */
    public static Map<UUID, LocalDateTime> getPlayersInAfkPond() {
        return Collections.unmodifiableMap(_playersInAfkPond);
    }

    public static void addAfkRewardTime(UUID playerId, String rewardKey, LocalDateTime time) {
       if  (_playersAfkRewards.containsKey(playerId)) {
              _playersAfkRewards.get(playerId).put(rewardKey, time);
         } else {
              Map<String, LocalDateTime> rewards = new HashMap<>();
              rewards.put(rewardKey, time);
              _playersAfkRewards.put(playerId, rewards);
       }
    }

    public static void removeAfkRewardTime(UUID playerId, String rewardKey) {
        Map<String, LocalDateTime> rewards = _playersAfkRewards.get(playerId);
        if (rewards != null) {
            rewards.remove(rewardKey);
            if (rewards.isEmpty()) {
                _playersAfkRewards.remove(playerId);
            }
        }
    }

    public static void clearAfkRewardTimes(UUID playerId) {
        _playersAfkRewards.remove(playerId);
    }

    public static @Nullable LocalDateTime getAfkRewardTime(UUID playerId, String rewardKey) {
        Map<String, LocalDateTime> rewards = _playersAfkRewards.get(playerId);
        if (rewards != null) {
            return rewards.get(rewardKey);
        }
        return null;
    }
    //#endregion

    //#region Join Time

    /**
     * Adds a player's join time.
     *
     * @param playerId The unique identifier of the player.
     * @param time     The time the player joined.
     */
    public static void addJoinTime(UUID playerId, LocalDateTime time) {
        _playerNextGameRewardTime.put(playerId, time);
    }

    /**
     * Removes a player's join time.
     *
     * @param playerId The unique identifier of the player.
     */
    public static void removeJoinTime(UUID playerId) {
        if (!_playerNextGameRewardTime.containsKey(playerId))
            return;
        _playerNextGameRewardTime.remove(playerId);
    }

    /**
     * Retrieves the join time of a player.
     *
     * @param playerId The unique identifier of the player.
     * @return The join time of the player, or null if no join time is recorded.
     */
    public static @Nullable LocalDateTime getJoinTime(UUID playerId) {
        return _playerNextGameRewardTime.get(playerId);
    }

    /**
     * Retrieves an unmodifiable map of players and their respective join times.
     *
     * @return An unmodifiable map of players' join times.
     */
    public static Map<UUID, LocalDateTime> getPlayersJoinTime() {
        return Collections.unmodifiableMap(_playerNextGameRewardTime);
    }
    //#endregion
}