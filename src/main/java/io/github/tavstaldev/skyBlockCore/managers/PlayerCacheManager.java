package io.github.tavstaldev.skyBlockCore.managers;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

public class PlayerCacheManager {
    private static final Map<UUID, LocalDateTime> _playersInAfkPond = new HashMap<>();
    private static final Map<UUID, LocalDateTime> _playersJoinTime = new HashMap<>();

    //#region Protections
    public static void addToAfkPond(UUID playerId, LocalDateTime time) {
        _playersInAfkPond.put(playerId, time);
    }

    public static void removeFromAfkPond(UUID playerId) {
        _playersInAfkPond.remove(playerId);
    }

    public static @Nullable LocalDateTime getAfkTime(UUID playerId) {
        return _playersInAfkPond.get(playerId);
    }

    public static Map<UUID, LocalDateTime> getPlayersInAfkPond() {
        return Collections.unmodifiableMap(_playersInAfkPond);
    }
    //#endregion

    //#region Join Time
    public static void addJoinTime(UUID playerId, LocalDateTime time) {
        _playersJoinTime.put(playerId, time);
    }

    public static void removeJoinTime(UUID playerId) {
        _playersJoinTime.remove(playerId);
    }

    public static @Nullable LocalDateTime getJoinTime(UUID playerId) {
        return _playersJoinTime.get(playerId);
    }

    public static Map<UUID, LocalDateTime> getPlayersJoinTime() {
        return Collections.unmodifiableMap(_playersJoinTime);
    }
    //#endregion
}