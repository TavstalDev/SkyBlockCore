package io.github.tavstaldev.skyBlockCore.managers;

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
    // Stores the join times of players.
    private static final Map<UUID, LocalDateTime> _playersJoinTime = new HashMap<>();

    //#region Protections

    /**
     * Adds a player to the AFK pond with the specified time.
     *
     * @param playerId The unique identifier of the player.
     * @param time     The time the player entered the AFK pond.
     */
    public static void addToAfkPond(UUID playerId, LocalDateTime time) {
        _playersInAfkPond.put(playerId, time);
    }

    /**
     * Removes a player from the AFK pond.
     *
     * @param playerId The unique identifier of the player.
     */
    public static void removeFromAfkPond(UUID playerId) {
        _playersInAfkPond.remove(playerId);
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
    //#endregion

    //#region Join Time

    /**
     * Adds a player's join time.
     *
     * @param playerId The unique identifier of the player.
     * @param time     The time the player joined.
     */
    public static void addJoinTime(UUID playerId, LocalDateTime time) {
        _playersJoinTime.put(playerId, time);
    }

    /**
     * Removes a player's join time.
     *
     * @param playerId The unique identifier of the player.
     */
    public static void removeJoinTime(UUID playerId) {
        _playersJoinTime.remove(playerId);
    }

    /**
     * Retrieves the join time of a player.
     *
     * @param playerId The unique identifier of the player.
     * @return The join time of the player, or null if no join time is recorded.
     */
    public static @Nullable LocalDateTime getJoinTime(UUID playerId) {
        return _playersJoinTime.get(playerId);
    }

    /**
     * Retrieves an unmodifiable map of players and their respective join times.
     *
     * @return An unmodifiable map of players' join times.
     */
    public static Map<UUID, LocalDateTime> getPlayersJoinTime() {
        return Collections.unmodifiableMap(_playersJoinTime);
    }
    //#endregion
}